package dev.mooka.translationnexus.domain.model;

import dev.mooka.translationnexus.shared.PlaceholderValidator;
import dev.mooka.translationnexus.domain.enums.TranslationStatusEnum;
import dev.mooka.translationnexus.domain.enums.HistoryActionEnum;
import dev.mooka.translationnexus.exception.BusinessException;
import dev.mooka.translationnexus.exception.impl.NoTranslationForLocaleException;
import dev.mooka.translationnexus.exception.impl.PlaceholderMismatchException;
import lombok.*;

import java.time.Instant;
import java.util.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationModel {

    private String id;
    private String keyCode;
    private String version;
    private String category;
    @Builder.Default
    private List<String> tags = new ArrayList<>();
    private String contextInfo;
    private String baseValue;
    @Builder.Default
    private Map<String, TranslationValueModel> translations = new HashMap<>();
    @Builder.Default
    private List<HistoryEntryModel> history = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

    // ─── Business Rules ──────────────────────────────────────────────────────

    public void updateTranslation(String locale, String value, String username, boolean isReviewer)
            throws BusinessException {
        // Validate placeholders
        if (!PlaceholderValidator.isValid(this.baseValue, value)) {
            throw new PlaceholderMismatchException(PlaceholderValidator.extract(this.baseValue).toString());
        }

        TranslationValueModel existing = this.translations.get(locale);
        String previousValue = existing != null ? existing.getTranslatedValue() : null;

        // Any edit/save operation moves the translation to REVIEW status
        TranslationStatusEnum newStatus = TranslationStatusEnum.REVIEW;

        this.translations.put(locale, TranslationValueModel.builder()
                .translatedValue(value)
                .status(newStatus)
                .lastModifiedBy(username)
                .updatedAt(Instant.now())
                .build());

        this.history.add(HistoryEntryModel.builder()
                .locale(locale)
                .modifiedBy(username)
                .previousValue(previousValue)
                .newValue(value)
                .action(HistoryActionEnum.EDIT)
                .timestamp(Instant.now())
                .build());

        this.updatedAt = Instant.now();
    }

    public void approveTranslation(String locale, String username) throws BusinessException {
        TranslationValueModel val = this.translations.get(locale);
        if (val == null) {
            throw new NoTranslationForLocaleException(locale);
        }

        String currentValue = val.getTranslatedValue();
        val.setStatus(TranslationStatusEnum.APPROVED);
        val.setLastModifiedBy(username);
        val.setUpdatedAt(Instant.now());

        this.history.add(HistoryEntryModel.builder()
                .locale(locale)
                .modifiedBy(username)
                .previousValue(currentValue)
                .newValue(currentValue)
                .action(HistoryActionEnum.APPROVE)
                .timestamp(Instant.now())
                .build());

        this.updatedAt = Instant.now();
    }

    public void updateStatus(String locale, TranslationStatusEnum status, String username) throws BusinessException {
        TranslationValueModel val = this.translations.get(locale);
        if (val == null) {
            throw new NoTranslationForLocaleException(locale);
        }

        TranslationStatusEnum prevStatus = val.getStatus();
        if (prevStatus != status) {
            val.setStatus(status);
            val.setLastModifiedBy(username);
            val.setUpdatedAt(Instant.now());

            this.history.add(HistoryEntryModel.builder()
                    .locale(locale)
                    .modifiedBy(username)
                    .previousValue(val.getTranslatedValue())
                    .newValue(val.getTranslatedValue())
                    .action(status == TranslationStatusEnum.APPROVED ? HistoryActionEnum.APPROVE : HistoryActionEnum.EDIT)
                    .timestamp(Instant.now())
                    .build());

            this.updatedAt = Instant.now();
        }
    }
}
