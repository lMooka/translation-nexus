package dev.mooka.translationnexus.service;

import dev.mooka.translationnexus.core.PlaceholderValidator;
import dev.mooka.translationnexus.domain.HistoryEntry;
import dev.mooka.translationnexus.domain.TranslationDocument;
import dev.mooka.translationnexus.domain.TranslationValue;
import dev.mooka.translationnexus.domain.AppVersion;
import dev.mooka.translationnexus.exception.impl.*;
import dev.mooka.translationnexus.repository.TranslationRepository;
import dev.mooka.translationnexus.resource.dto.TranslationKeyCreateDTO;
import dev.mooka.translationnexus.resource.dto.TranslationUpdateDTO;
import dev.mooka.translationnexus.exception.BusinessException;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranslationService {

    private final TranslationRepository translationRepository;
    private final MongoTemplate mongoTemplate;
    private final MeterRegistry meterRegistry;
    private final CategoryService categoryService;
    private final VersionService versionService;

    // ─── Key Management ──────────────────────────────────────────────────────

    public TranslationDocument createKey(TranslationKeyCreateDTO dto) throws BusinessException {
        AppVersion activeVersion = versionService.getActiveVersion()
                .orElseThrow(() -> new GenericBusinessException("No active version configured."));
        String version = activeVersion.getVersion();

        categoryService.validateCategoryAndPath(dto.category(), dto.keyCode());

        if (translationRepository.existsByKeyCodeAndVersion(dto.keyCode(), version)) {
            throw new TranslationKeyAlreadyExistsException(dto.keyCode(), version);
        }

        TranslationDocument doc = TranslationDocument.builder()
                .keyCode(dto.keyCode())
                .version(version)
                .category(dto.category())
                .tags(dto.tags() != null ? dto.tags() : new ArrayList<>())
                .contextInfo(dto.contextInfo())
                .baseValue(dto.baseValue())
                .translations(new HashMap<>())
                .history(new ArrayList<>())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        log.info("Creating translation key: {} for version: {}", dto.keyCode(), version);
        meterRegistry.counter("translation_keys_created_total").increment();

        return translationRepository.save(doc);
    }

    // ─── Listing & Filtering ─────────────────────────────────────────────────

    public Page<TranslationDocument> findAll(String version, String tag, String category,
                                             String search, Pageable pageable) {
        Query query = new Query();

        if (version != null && !version.isBlank()) {
            query.addCriteria(Criteria.where("version").is(version));
        }
        if (tag != null && !tag.isBlank()) {
            query.addCriteria(Criteria.where("tags").in(tag));
        }
        if (category != null && !category.isBlank()) {
            query.addCriteria(Criteria.where("category").is(category));
        }
        if (search != null && !search.isBlank()) {
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("keyCode").regex(search, "i"),
                    Criteria.where("baseValue").regex(search, "i")
            ));
        }

        long total = mongoTemplate.count(query, TranslationDocument.class);
        query.with(pageable);
        List<TranslationDocument> docs = mongoTemplate.find(query, TranslationDocument.class);

        return new PageImpl<>(docs, pageable, total);
    }

    // ─── Translation Edit ─────────────────────────────────────────────────────

    public TranslationDocument updateTranslation(String id, String locale,
                                                  TranslationUpdateDTO dto,
                                                  String username, boolean isReviewer) throws BusinessException {
        TranslationDocument doc = translationRepository.findById(id)
                .orElseThrow(TranslationNotFoundException::new);

        AppVersion activeVersion = versionService.getActiveVersion()
                .orElseThrow(() -> new GenericBusinessException("No active version configured."));
        if (!doc.getVersion().equals(activeVersion.getVersion())) {
            throw new VersionLockedException();
        }

        // Validate placeholders
        if (!PlaceholderValidator.isValid(doc.getBaseValue(), dto.value())) {
            log.warn("Placeholder validation failed for key: {} and locale: {}. Expected placeholders: {}", doc.getKeyCode(), locale, PlaceholderValidator.extract(doc.getBaseValue()));
            meterRegistry.counter("translation_validation_failures_total", "locale", locale).increment();
            throw new PlaceholderMismatchException(PlaceholderValidator.extract(doc.getBaseValue()).toString());
        }

        TranslationValue existing = doc.getTranslations().get(locale);
        String previousValue = existing != null ? existing.getTranslatedValue() : null;

        // Any edit/save operation moves the translation to REVIEW status
        String newStatus = "REVIEW";

        doc.getTranslations().put(locale, TranslationValue.builder()
                .translatedValue(dto.value())
                .status(newStatus)
                .lastModifiedBy(username)
                .updatedAt(Instant.now())
                .build());

        doc.getHistory().add(HistoryEntry.builder()
                .locale(locale)
                .modifiedBy(username)
                .previousValue(previousValue)
                .newValue(dto.value())
                .action("EDIT")
                .timestamp(Instant.now())
                .build());

        doc.setUpdatedAt(Instant.now());

        log.info("Translation updated for key: {}, locale: {}, by user: {}", doc.getKeyCode(), locale, username);
        meterRegistry.counter("translation_updates_total", "locale", locale).increment();

        return translationRepository.save(doc);
    }

    // ─── Approval ─────────────────────────────────────────────────────────────

    /**
     * Returns all documents that have at least one locale with REVIEW status.
     * Filtered in-memory; acceptable for small datasets.
     */
    public List<TranslationDocument> findPending() {
        return translationRepository.findAll().stream()
                .filter(doc -> doc.getTranslations().values().stream()
                        .anyMatch(tv -> "REVIEW".equals(tv.getStatus())))
                .toList();
    }

    public TranslationDocument approveTranslation(String id, String locale, String username) throws BusinessException {
        TranslationDocument doc = translationRepository.findById(id)
                .orElseThrow(TranslationNotFoundException::new);

        AppVersion activeVersion = versionService.getActiveVersion()
                .orElseThrow(() -> new GenericBusinessException("No active version configured."));
        if (!doc.getVersion().equals(activeVersion.getVersion())) {
            throw new VersionLockedException();
        }

        TranslationValue val = doc.getTranslations().get(locale);
        if (val == null) {
            throw new NoTranslationForLocaleException(locale);
        }

        String currentValue = val.getTranslatedValue();
        val.setStatus("APPROVED");
        val.setLastModifiedBy(username);
        val.setUpdatedAt(Instant.now());

        doc.getHistory().add(HistoryEntry.builder()
                .locale(locale)
                .modifiedBy(username)
                .previousValue(currentValue)
                .newValue(currentValue)
                .action("APPROVE")
                .timestamp(Instant.now())
                .build());

        doc.setUpdatedAt(Instant.now());

        log.info("Translation approved for key: {}, locale: {}, by user: {}", doc.getKeyCode(), locale, username);
        meterRegistry.counter("translation_approvals_total", "locale", locale).increment();

        return translationRepository.save(doc);
    }

    // ─── History ──────────────────────────────────────────────────────────────

    public List<HistoryEntry> getHistory(String id) {
        TranslationDocument doc = translationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Translation key not found"));
        return doc.getHistory();
    }

    public TranslationDocument updateStatus(String id, String locale, String status, String username) throws BusinessException {
        TranslationDocument doc = translationRepository.findById(id)
                .orElseThrow(TranslationNotFoundException::new);

        AppVersion activeVersion = versionService.getActiveVersion()
                .orElseThrow(() -> new GenericBusinessException("No active version configured."));
        if (!doc.getVersion().equals(activeVersion.getVersion())) {
            throw new VersionLockedException();
        }

        TranslationValue val = doc.getTranslations().get(locale);
        if (val == null) {
            throw new NoTranslationForLocaleException(locale);
        }

        String prevStatus = val.getStatus();
        if (!prevStatus.equals(status)) {
            val.setStatus(status);
            val.setLastModifiedBy(username);
            val.setUpdatedAt(Instant.now());

            doc.getHistory().add(HistoryEntry.builder()
                    .locale(locale)
                    .modifiedBy(username)
                    .previousValue(val.getTranslatedValue())
                    .newValue(val.getTranslatedValue())
                    .action(status.equals("APPROVED") ? "APPROVE" : "EDIT")
                    .timestamp(Instant.now())
                    .build());

            doc.setUpdatedAt(Instant.now());
            return translationRepository.save(doc);
        }

        return doc;
    }

    public void deleteKey(String id) throws BusinessException {
        TranslationDocument doc = translationRepository.findById(id)
                .orElseThrow(TranslationNotFoundException::new);
        AppVersion activeVersion = versionService.getActiveVersion()
                .orElseThrow(() -> new GenericBusinessException("No active version configured."));
        if (!doc.getVersion().equals(activeVersion.getVersion())) {
            throw new VersionLockedException();
        }
        translationRepository.delete(doc);
        log.info("Translation key deleted: {}", doc.getKeyCode());
    }
}
