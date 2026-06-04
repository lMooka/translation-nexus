package dev.mooka.translationnexus.service;

import dev.mooka.translationnexus.shared.PlaceholderValidator;
import dev.mooka.translationnexus.domain.enums.TranslationStatusEnum;
import dev.mooka.translationnexus.domain.entity.TranslationEntity;
import dev.mooka.translationnexus.domain.model.TranslationModel;
import dev.mooka.translationnexus.domain.entity.HistoryEntryEntity;
import dev.mooka.translationnexus.domain.entity.AppVersionEntity;
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
    private final MapperService mapperService;

    // ─── Key Management ──────────────────────────────────────────────────────

    public TranslationEntity createKey(TranslationKeyCreateDTO dto) throws BusinessException {
        AppVersionEntity activeVersion = versionService.getActiveVersion()
                .orElseThrow(() -> new GenericBusinessException("No active version configured."));
        String version = activeVersion.getVersion();

        categoryService.validateCategoryAndPath(dto.category(), dto.keyCode());

        if (translationRepository.existsByKeyCodeAndVersion(dto.keyCode(), version)) {
            throw new TranslationKeyAlreadyExistsException(dto.keyCode(), version);
        }

        TranslationEntity entity = TranslationEntity.builder()
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

        return translationRepository.save(entity);
    }

    // ─── Listing & Filtering ─────────────────────────────────────────────────

    public Page<TranslationEntity> findAll(String version, List<String> tags, String category,
            String search, Pageable pageable) {
        Query query = new Query();

        if (version != null && !version.isBlank()) {
            query.addCriteria(Criteria.where("version").is(version));
        }
        if (tags != null && !tags.isEmpty()) {
            List<String> cleanTags = tags.stream().filter(t -> t != null && !t.isBlank()).toList();
            if (!cleanTags.isEmpty()) {
                query.addCriteria(Criteria.where("tags").in(cleanTags));
            }
        }
        if (category != null && !category.isBlank()) {
            query.addCriteria(Criteria.where("category").is(category));
        }
        if (search != null && !search.isBlank()) {
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("keyCode").regex(search, "i"),
                    Criteria.where("baseValue").regex(search, "i")));
        }

        long total = mongoTemplate.count(query, TranslationEntity.class);
        query.with(pageable);
        List<TranslationEntity> docs = mongoTemplate.find(query, TranslationEntity.class);

        return new PageImpl<>(docs, pageable, total);
    }

    // ─── Translation Edit ─────────────────────────────────────────────────────

    public TranslationEntity updateTranslation(String id, String locale,
            TranslationUpdateDTO dto,
            String username, boolean isReviewer) throws BusinessException {
        TranslationEntity entity = translationRepository.findById(id)
                .orElseThrow(TranslationNotFoundException::new);

        AppVersionEntity activeVersion = versionService.getActiveVersion()
                .orElseThrow(() -> new GenericBusinessException("No active version configured."));
        if (!entity.getVersion().equals(activeVersion.getVersion())) {
            throw new VersionLockedException();
        }

        // Map to domain model
        TranslationModel model = mapperService.toModel(entity);

        // Apply domain rule inside model
        try {
            model.updateTranslation(locale, dto.value(), username, isReviewer);
        } catch (PlaceholderMismatchException e) {
            log.warn("Placeholder validation failed for key: {} and locale: {}. Expected placeholders: {}",
                    model.getKeyCode(), locale, PlaceholderValidator.extract(model.getBaseValue()));
            meterRegistry.counter("translation_validation_failures_total", "locale", locale).increment();
            throw e;
        }

        // Map back to entity and save
        TranslationEntity updatedEntity = mapperService.toEntity(model);

        log.info("Translation updated for key: {}, locale: {}, by user: {}", model.getKeyCode(), locale, username);
        meterRegistry.counter("translation_updates_total", "locale", locale).increment();

        return translationRepository.save(updatedEntity);
    }

    // ─── Approval ─────────────────────────────────────────────────────────────

    /**
     * Returns all documents that have at least one locale with REVIEW status.
     * Filtered in-memory; acceptable for small datasets.
     */
    public List<TranslationEntity> findPending() {
        return translationRepository.findAll().stream()
                .filter(doc -> doc.getTranslations().values().stream()
                        .anyMatch(tv -> tv.getStatus() == TranslationStatusEnum.REVIEW))
                .toList();
    }

    public TranslationEntity approveTranslation(String id, String locale, String username) throws BusinessException {
        TranslationEntity entity = translationRepository.findById(id)
                .orElseThrow(TranslationNotFoundException::new);

        AppVersionEntity activeVersion = versionService.getActiveVersion()
                .orElseThrow(() -> new GenericBusinessException("No active version configured."));
        if (!entity.getVersion().equals(activeVersion.getVersion())) {
            throw new VersionLockedException();
        }

        TranslationModel model = mapperService.toModel(entity);
        model.approveTranslation(locale, username);

        TranslationEntity updatedEntity = mapperService.toEntity(model);

        log.info("Translation approved for key: {}, locale: {}, by user: {}", model.getKeyCode(), locale, username);
        meterRegistry.counter("translation_approvals_total", "locale", locale).increment();

        return translationRepository.save(updatedEntity);
    }

    // ─── History ──────────────────────────────────────────────────────────────

    public List<HistoryEntryEntity> getHistory(String id) {
        TranslationEntity entity = translationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Translation key not found"));
        return entity.getHistory();
    }

    public TranslationEntity updateStatus(String id, String locale, TranslationStatusEnum status, String username)
            throws BusinessException {
        TranslationEntity entity = translationRepository.findById(id)
                .orElseThrow(TranslationNotFoundException::new);

        AppVersionEntity activeVersion = versionService.getActiveVersion()
                .orElseThrow(() -> new GenericBusinessException("No active version configured."));
        if (!entity.getVersion().equals(activeVersion.getVersion())) {
            throw new VersionLockedException();
        }

        TranslationModel model = mapperService.toModel(entity);
        model.updateStatus(locale, status, username);

        TranslationEntity updatedEntity = mapperService.toEntity(model);
        return translationRepository.save(updatedEntity);
    }

    public void deleteKey(String id) throws BusinessException {
        TranslationEntity entity = translationRepository.findById(id)
                .orElseThrow(TranslationNotFoundException::new);
        AppVersionEntity activeVersion = versionService.getActiveVersion()
                .orElseThrow(() -> new GenericBusinessException("No active version configured."));
        if (!entity.getVersion().equals(activeVersion.getVersion())) {
            throw new VersionLockedException();
        }
        translationRepository.delete(entity);
        log.info("Translation key deleted: {}", entity.getKeyCode());
    }
}
