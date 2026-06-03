package dev.mooka.translationnexus.service;

import dev.mooka.translationnexus.core.PlaceholderValidator;
import dev.mooka.translationnexus.core.CategoryValidationHelper;
import dev.mooka.translationnexus.domain.HistoryEntry;
import dev.mooka.translationnexus.domain.TranslationDocument;
import dev.mooka.translationnexus.domain.TranslationValue;
import dev.mooka.translationnexus.exception.impl.*;
import dev.mooka.translationnexus.repository.TranslationRepository;
import dev.mooka.translationnexus.resource.dto.ImportResultDTO;
import dev.mooka.translationnexus.resource.dto.TranslationKeyCreateDTO;
import dev.mooka.translationnexus.resource.dto.TranslationUpdateDTO;
import dev.mooka.translationnexus.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

import dev.mooka.translationnexus.domain.Locale;
import dev.mooka.translationnexus.repository.LocaleRepository;
import dev.mooka.translationnexus.domain.AppVersion;
import dev.mooka.translationnexus.repository.AppVersionRepository;
import dev.mooka.translationnexus.domain.Category;
import dev.mooka.translationnexus.domain.PathMapping;
import dev.mooka.translationnexus.repository.CategoryRepository;

@Service
@RequiredArgsConstructor
public class TranslationService {

    private final TranslationRepository translationRepository;
    private final LocaleRepository localeRepository;
    private final AppVersionRepository appVersionRepository;
    private final CategoryRepository categoryRepository;
    private final MongoTemplate mongoTemplate;

    // ─── Validation Helpers ──────────────────────────────────────────────────

    private void validateCategoryAndPath(String categoryName, String keyCode) throws BusinessException {
        Category category = categoryRepository.findByNameIgnoreCase(categoryName)
                .orElseThrow(() -> new CategoryNotFoundException(categoryName));

        boolean matched = false;
        if (category.getPathMappings() != null) {
            for (PathMapping pm : category.getPathMappings()) {
                if (CategoryValidationHelper.matchPath(keyCode, pm.getPattern())) {
                    matched = true;
                    break;
                }
            }
        }

        if (!matched) {
            throw new InvalidKeyPathException(keyCode, categoryName);
        }
    }

    // ─── Key Management ──────────────────────────────────────────────────────

    public TranslationDocument createKey(TranslationKeyCreateDTO dto) throws BusinessException {
        AppVersion activeVersion = appVersionRepository.findByActiveTrue()
                .orElseThrow(() -> new GenericBusinessException("No active version configured."));
        String version = activeVersion.getVersion();

        validateCategoryAndPath(dto.category(), dto.keyCode());

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

        AppVersion activeVersion = appVersionRepository.findByActiveTrue()
                .orElseThrow(() -> new GenericBusinessException("No active version configured."));
        if (!doc.getVersion().equals(activeVersion.getVersion())) {
            throw new VersionLockedException();
        }

        // Validate placeholders
        if (!PlaceholderValidator.isValid(doc.getBaseValue(), dto.value())) {
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

        AppVersion activeVersion = appVersionRepository.findByActiveTrue()
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

        return translationRepository.save(doc);
    }

    // ─── History ──────────────────────────────────────────────────────────────

    public List<HistoryEntry> getHistory(String id) {
        TranslationDocument doc = translationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Translation key not found"));
        return doc.getHistory();
    }

    // ─── CSV Import ───────────────────────────────────────────────────────────

    /**
     * Parses a localization CSV and upserts TranslationDocuments.
     *
     * <p>Column mapping:
     * <ul>
     *   <li>Weblate Key       → keyCode</li>
     *   <li>Category          → category</li>
     *   <li>Internal Name     → tag "entity:&lt;value&gt;"</li>
     *   <li>Field             → tag "field:&lt;value&gt;"</li>
     *   <li>English           → baseValue + locale "en" (APPROVED)</li>
     *   <li>Português         → locale "pt" (status from Review Status)</li>
     *   <li>Review Status     → "Approved" = APPROVED, else = PENDING_APPROVAL</li>
     * </ul>
     *
     * <p>Version is fixed to for all rows.
     */
    public ImportResultDTO importCsv(MultipartFile file, String importedBy) throws BusinessException {
        AppVersion activeVersion = appVersionRepository.findByActiveTrue()
                .orElseThrow(() -> new GenericBusinessException("No active version configured."));
        String importVersion = activeVersion.getVersion();

        List<TranslationDocument> toSave = new ArrayList<>();
        List<String> skippedKeyCodes = new ArrayList<>();
        int totalRows = 0;

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .build();

        try (CSVParser parser = new CSVParser(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8), format)) {

            for (CSVRecord row : parser) {
                totalRows++;

                String keyCode      = row.get("Weblate Key");
                String category     = row.get("Category");
                String internalName = row.get("Internal Name");
                String field        = row.get("Field");
                String english      = row.get("English");
                String portuguese   = row.get("Português");
                String reviewStatus = row.get("Review Status");

                if (keyCode == null || keyCode.isBlank()) {
                    // Malformed row — skip silently
                    skippedKeyCodes.add("<blank key at row " + totalRows + ">");
                    continue;
                }

                validateCategoryAndPath(category, keyCode);

                boolean isApproved = "Approved".equalsIgnoreCase(reviewStatus);
                boolean hasPtValue = portuguese != null && !portuguese.isBlank();

                String ptStatus;
                if (isApproved) {
                    ptStatus = "APPROVED";
                } else if (hasPtValue) {
                    ptStatus = "REVIEW";
                } else {
                    ptStatus = "PENDING";
                }

                // Skip creating a pt entry when approved but value is blank (data inconsistency)
                if (isApproved && !hasPtValue) {
                    skippedKeyCodes.add(keyCode);
                    // Still upsert the document itself with English — just omit pt entry
                }

                // Derive tags
                List<String> tags = new ArrayList<>();
                if (internalName != null && !internalName.isBlank()) {
                    tags.add("entity:" + internalName);
                }
                if (field != null && !field.isBlank()) {
                    tags.add("field:" + field);
                }

                Instant now = Instant.now();

                // Upsert: find existing doc or build a new one
                TranslationDocument doc = translationRepository
                        .findByKeyCodeAndVersion(keyCode, importVersion)
                        .orElseGet(() -> TranslationDocument.builder()
                                .keyCode(keyCode)
                                .version(importVersion)
                                .translations(new HashMap<>())
                                .history(new ArrayList<>())
                                .createdAt(now)
                                .build());

                // Always update metadata
                doc.setCategory(category);
                doc.setTags(tags);
                doc.setUpdatedAt(now);

                // English → baseValue + "en" locale entry (always APPROVED)
                if (english != null && !english.isBlank()) {
                    String prevEn = doc.getTranslations().containsKey("en")
                            ? doc.getTranslations().get("en").getTranslatedValue() : null;
                    doc.setBaseValue(english);
                    doc.getTranslations().put("en", TranslationValue.builder()
                            .translatedValue(english)
                            .status("APPROVED")
                            .lastModifiedBy(importedBy)
                            .updatedAt(now)
                            .build());
                    doc.getHistory().add(HistoryEntry.builder()
                            .locale("en")
                            .modifiedBy(importedBy)
                            .previousValue(prevEn)
                            .newValue(english)
                            .action("IMPORT")
                            .timestamp(now)
                            .build());
                }

                // Portuguese → "pt" locale entry (only when value is present OR status is PENDING)
                if (hasPtValue || !isApproved) {
                    String prevPt = doc.getTranslations().containsKey("pt")
                            ? doc.getTranslations().get("pt").getTranslatedValue() : null;
                    doc.getTranslations().put("pt", TranslationValue.builder()
                            .translatedValue(hasPtValue ? portuguese : null)
                            .status(ptStatus)
                            .lastModifiedBy(importedBy)
                            .updatedAt(now)
                            .build());
                    doc.getHistory().add(HistoryEntry.builder()
                            .locale("pt")
                            .modifiedBy(importedBy)
                            .previousValue(prevPt)
                            .newValue(hasPtValue ? portuguese : null)
                            .action("IMPORT")
                            .timestamp(now)
                            .build());
                }

                toSave.add(doc);
            }

        } catch (IOException e) {
            throw new CsvParseException(e.getMessage());
        }

        translationRepository.saveAll(toSave);

        return new ImportResultDTO(
                totalRows,
                toSave.size(),
                skippedKeyCodes.size(),
                skippedKeyCodes
        );
    }

    public TranslationDocument updateStatus(String id, String locale, String status, String username) throws BusinessException {
        TranslationDocument doc = translationRepository.findById(id)
                .orElseThrow(TranslationNotFoundException::new);

        AppVersion activeVersion = appVersionRepository.findByActiveTrue()
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

    public List<Locale> getAllLocales() {
        return localeRepository.findAll();
    }

    public List<AppVersion> getAllVersions() {
        return appVersionRepository.findAll();
    }

    public AppVersion createVersion(String newVersionName) throws BusinessException {
        if (newVersionName == null || newVersionName.isBlank()) {
            throw new GenericBusinessException("Version name cannot be empty");
        }
        String trimmedVersionName = newVersionName.trim();
        if (appVersionRepository.existsByVersion(trimmedVersionName)) {
            throw new GenericBusinessException("Version " + trimmedVersionName + " already exists");
        }

        // 1. Get current active version
        AppVersion currentActive = appVersionRepository.findByActiveTrue()
                .orElse(null);

        // 2. Deactivate current active version
        if (currentActive != null) {
            currentActive.setActive(false);
            appVersionRepository.save(currentActive);
        }

        // 3. Create and activate the new version
        AppVersion newActive = AppVersion.builder()
                .version(trimmedVersionName)
                .active(true)
                .createdAt(Instant.now())
                .build();
        newActive = appVersionRepository.save(newActive);

        // 4. Clone all translation documents from old active version
        if (currentActive != null) {
            List<TranslationDocument> oldDocs = translationRepository.findAll().stream()
                    .filter(doc -> currentActive.getVersion().equals(doc.getVersion()))
                    .toList();

            List<TranslationDocument> clonedDocs = new ArrayList<>();
            for (TranslationDocument doc : oldDocs) {
                // Clone translations map
                Map<String, TranslationValue> clonedTranslations = new HashMap<>();
                if (doc.getTranslations() != null) {
                    for (Map.Entry<String, TranslationValue> entry : doc.getTranslations().entrySet()) {
                        TranslationValue tv = entry.getValue();
                        clonedTranslations.put(entry.getKey(), TranslationValue.builder()
                                .translatedValue(tv.getTranslatedValue())
                                .status(tv.getStatus())
                                .lastModifiedBy(tv.getLastModifiedBy())
                                .updatedAt(tv.getUpdatedAt())
                                .build());
                    }
                }

                // Clone history list
                List<HistoryEntry> clonedHistory = new ArrayList<>();
                if (doc.getHistory() != null) {
                    for (HistoryEntry he : doc.getHistory()) {
                        clonedHistory.add(HistoryEntry.builder()
                                .locale(he.getLocale())
                                .modifiedBy(he.getModifiedBy())
                                .previousValue(he.getPreviousValue())
                                .newValue(he.getNewValue())
                                .action(he.getAction())
                                .timestamp(he.getTimestamp())
                                .build());
                    }
                }

                TranslationDocument clonedDoc = TranslationDocument.builder()
                        .keyCode(doc.getKeyCode())
                        .version(trimmedVersionName)
                        .category(doc.getCategory())
                        .tags(doc.getTags() != null ? new ArrayList<>(doc.getTags()) : new ArrayList<>())
                        .contextInfo(doc.getContextInfo())
                        .baseValue(doc.getBaseValue())
                        .translations(clonedTranslations)
                        .history(clonedHistory)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();

                clonedDocs.add(clonedDoc);
            }
            translationRepository.saveAll(clonedDocs);
        }

        return newActive;
    }

    // ─── Category Management ──────────────────────────────────────────────────

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category createCategory(Category category) throws BusinessException {
        if (category == null || category.getName() == null || category.getName().isBlank()) {
            throw new GenericBusinessException("Category name cannot be empty");
        }
        String cleanName = category.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(cleanName)) {
            throw new GenericBusinessException("Category with name '" + cleanName + "' already exists");
        }

        List<PathMapping> mappings = new ArrayList<>();
        if (category.getPathMappings() != null) {
            for (PathMapping pm : category.getPathMappings()) {
                if (pm.getPattern() == null || pm.getPattern().isBlank()) {
                    throw new GenericBusinessException("Pattern cannot be empty");
                }
                if (pm.getFilename() == null || pm.getFilename().isBlank()) {
                    throw new GenericBusinessException("Filename cannot be empty");
                }
                if (!CategoryValidationHelper.hasExactlyOneWildcard(pm.getPattern())) {
                    throw new GenericBusinessException("Pattern '" + pm.getPattern() + "' must contain exactly one wildcard (*)");
                }
                String filename = pm.getFilename().trim();
                if (!filename.toLowerCase().endsWith(".csv")) {
                    filename += ".csv";
                }
                mappings.add(new PathMapping(pm.getPattern().trim(), filename));
            }
        }

        Category doc = Category.builder()
                .name(cleanName)
                .pathMappings(mappings)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return categoryRepository.save(doc);
    }

    public Category updateCategory(String id, Category category) throws BusinessException {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new GenericBusinessException(404, "exception.category.not-found", "Category not found"));

        if (category.getName() == null || category.getName().isBlank()) {
            throw new GenericBusinessException("Category name cannot be empty");
        }

        String cleanName = category.getName().trim();
        if (!existing.getName().equalsIgnoreCase(cleanName) && categoryRepository.existsByNameIgnoreCase(cleanName)) {
            throw new GenericBusinessException("Category with name '" + cleanName + "' already exists");
        }

        List<PathMapping> mappings = new ArrayList<>();
        if (category.getPathMappings() != null) {
            for (PathMapping pm : category.getPathMappings()) {
                if (pm.getPattern() == null || pm.getPattern().isBlank()) {
                    throw new GenericBusinessException("Pattern cannot be empty");
                }
                if (pm.getFilename() == null || pm.getFilename().isBlank()) {
                    throw new GenericBusinessException("Filename cannot be empty");
                }
                if (!CategoryValidationHelper.hasExactlyOneWildcard(pm.getPattern())) {
                    throw new GenericBusinessException("Pattern '" + pm.getPattern() + "' must contain exactly one wildcard (*)");
                }
                String filename = pm.getFilename().trim();
                if (!filename.toLowerCase().endsWith(".csv")) {
                    filename += ".csv";
                }
                mappings.add(new PathMapping(pm.getPattern().trim(), filename));
            }
        }

        existing.setName(cleanName);
        existing.setPathMappings(mappings);
        existing.setUpdatedAt(Instant.now());

        return categoryRepository.save(existing);
    }

    public void deleteCategory(String id) throws BusinessException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new GenericBusinessException(404, "exception.category.not-found", "Category not found"));
        
        long count = translationRepository.findAll().stream()
                .filter(doc -> category.getName().equalsIgnoreCase(doc.getCategory()))
                .count();
        if (count > 0) {
            throw new GenericBusinessException("Cannot delete category '" + category.getName() + "' because it is referenced by " + count + " translation keys.");
        }

        categoryRepository.delete(category);
    }

    public void deleteKey(String id) throws BusinessException {
        TranslationDocument doc = translationRepository.findById(id)
                .orElseThrow(TranslationNotFoundException::new);
        AppVersion activeVersion = appVersionRepository.findByActiveTrue()
                .orElseThrow(() -> new GenericBusinessException("No active version configured."));
        if (!doc.getVersion().equals(activeVersion.getVersion())) {
            throw new VersionLockedException();
        }
        translationRepository.delete(doc);
    }

    public Optional<Locale> getLocaleById(String id) {
        if (id == null) return Optional.empty();
        return localeRepository.findById(id.trim().toLowerCase());
    }

    public Locale createLocale(Locale locale) throws BusinessException {
        if (locale == null || locale.getId() == null || locale.getId().isBlank()) {
            throw new GenericBusinessException("Locale code (ID) cannot be empty");
        }
        if (locale.getName() == null || locale.getName().isBlank()) {
            throw new GenericBusinessException("Locale name cannot be empty");
        }
        String cleanId = locale.getId().trim().toLowerCase();
        if (localeRepository.existsById(cleanId)) {
            throw new GenericBusinessException("Locale code '" + cleanId + "' already exists");
        }
        Locale doc = Locale.builder()
                .id(cleanId)
                .name(locale.getName().trim())
                .googleCode(locale.getGoogleCode() != null ? locale.getGoogleCode().trim() : null)
                .build();
        return localeRepository.save(doc);
    }

    public Locale updateLocale(String id, Locale locale) throws BusinessException {
        Locale existing = localeRepository.findById(id)
                .orElseThrow(() -> new GenericBusinessException(404, "exception.locale.not-found", "Locale not found"));
        if (locale.getName() == null || locale.getName().isBlank()) {
            throw new GenericBusinessException("Locale name cannot be empty");
        }
        existing.setName(locale.getName().trim());
        existing.setGoogleCode(locale.getGoogleCode() != null ? locale.getGoogleCode().trim() : null);
        return localeRepository.save(existing);
    }

    public void deleteLocale(String id) throws BusinessException {
        Locale existing = localeRepository.findById(id)
                .orElseThrow(() -> new GenericBusinessException(404, "exception.locale.not-found", "Locale not found"));
        localeRepository.delete(existing);
    }
}
