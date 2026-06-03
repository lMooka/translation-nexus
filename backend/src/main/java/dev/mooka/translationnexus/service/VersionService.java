package dev.mooka.translationnexus.service;

import dev.mooka.translationnexus.domain.entity.AppVersionEntity;
import dev.mooka.translationnexus.domain.entity.HistoryEntryEntity;
import dev.mooka.translationnexus.domain.entity.TranslationEntity;
import dev.mooka.translationnexus.domain.entity.TranslationValueEntity;
import dev.mooka.translationnexus.exception.BusinessException;
import dev.mooka.translationnexus.exception.impl.GenericBusinessException;
import dev.mooka.translationnexus.repository.AppVersionRepository;
import dev.mooka.translationnexus.repository.TranslationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VersionService {

    private final AppVersionRepository appVersionRepository;
    private final TranslationRepository translationRepository;

    public List<AppVersionEntity> getAllVersions() {
        return appVersionRepository.findAll();
    }

    public Optional<AppVersionEntity> getActiveVersion() {
        return appVersionRepository.findByActiveTrue();
    }

    public AppVersionEntity createVersion(String newVersionName) throws BusinessException {
        if (newVersionName == null || newVersionName.isBlank()) {
            throw new GenericBusinessException("Version name cannot be empty");
        }
        String trimmedVersionName = newVersionName.trim();
        if (appVersionRepository.existsByVersion(trimmedVersionName)) {
            throw new GenericBusinessException("Version " + trimmedVersionName + " already exists");
        }

        // 1. Get current active version
        AppVersionEntity currentActive = appVersionRepository.findByActiveTrue()
                .orElse(null);

        // 2. Deactivate current active version
        if (currentActive != null) {
            currentActive.setActive(false);
            appVersionRepository.save(currentActive);
        }

        // 3. Create and activate the new version
        AppVersionEntity newActive = AppVersionEntity.builder()
                .version(trimmedVersionName)
                .active(true)
                .createdAt(Instant.now())
                .build();
        newActive = appVersionRepository.save(newActive);

        // 4. Clone all translation documents from old active version
        if (currentActive != null) {
            List<TranslationEntity> oldDocs = translationRepository.findAll().stream()
                    .filter(doc -> currentActive.getVersion().equals(doc.getVersion()))
                    .toList();

            List<TranslationEntity> clonedDocs = new ArrayList<>();
            for (TranslationEntity doc : oldDocs) {
                // Clone translations map
                Map<String, TranslationValueEntity> clonedTranslations = new HashMap<>();
                if (doc.getTranslations() != null) {
                    for (Map.Entry<String, TranslationValueEntity> entry : doc.getTranslations().entrySet()) {
                        TranslationValueEntity tv = entry.getValue();
                        clonedTranslations.put(entry.getKey(), TranslationValueEntity.builder()
                                .translatedValue(tv.getTranslatedValue())
                                .status(tv.getStatus())
                                .lastModifiedBy(tv.getLastModifiedBy())
                                .updatedAt(tv.getUpdatedAt())
                                .build());
                    }
                }

                // Clone history list
                List<HistoryEntryEntity> clonedHistory = new ArrayList<>();
                if (doc.getHistory() != null) {
                    for (HistoryEntryEntity he : doc.getHistory()) {
                        clonedHistory.add(HistoryEntryEntity.builder()
                                .locale(he.getLocale())
                                .modifiedBy(he.getModifiedBy())
                                .previousValue(he.getPreviousValue())
                                .newValue(he.getNewValue())
                                .action(he.getAction())
                                .timestamp(he.getTimestamp())
                                .build());
                    }
                }

                TranslationEntity clonedDoc = TranslationEntity.builder()
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

        log.info("App Version created and active: {}", trimmedVersionName);
        return newActive;
    }
}
