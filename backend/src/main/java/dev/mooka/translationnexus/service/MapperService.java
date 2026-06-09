package dev.mooka.translationnexus.service;

import dev.mooka.translationnexus.domain.entity.*;
import dev.mooka.translationnexus.domain.model.*;
import dev.mooka.translationnexus.resource.dto.*;
import dev.mooka.translationnexus.exception.BusinessException;
import dev.mooka.translationnexus.domain.enums.HistoryActionEnum;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MapperService {

    // ─── Locale Mappings ─────────────────────────────────────────────────────

    public LocaleDTO toDTO(LocaleEntity entity) {
        if (entity == null) return null;
        return LocaleDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .googleCode(entity.getGoogleCode())
                .sortOrder(entity.getSortOrder())
                .build();
    }

    public LocaleEntity toEntity(LocaleDTO dto) {
        if (dto == null) return null;
        return LocaleEntity.builder()
                .id(dto.getId())
                .name(dto.getName())
                .googleCode(dto.getGoogleCode())
                .sortOrder(dto.getSortOrder())
                .build();
    }

    public LocaleModel toModel(LocaleEntity entity) {
        if (entity == null) return null;
        return LocaleModel.builder()
                .id(entity.getId())
                .name(entity.getName())
                .googleCode(entity.getGoogleCode())
                .sortOrder(entity.getSortOrder())
                .build();
    }

    public LocaleEntity toEntity(LocaleModel model) {
        if (model == null) return null;
        return LocaleEntity.builder()
                .id(model.getId())
                .name(model.getName())
                .googleCode(model.getGoogleCode())
                .sortOrder(model.getSortOrder())
                .build();
    }

    // ─── PathMapping Mappings ────────────────────────────────────────────────

    public PathMappingDTO toDTO(PathMappingEntity entity) {
        if (entity == null) return null;
        return PathMappingDTO.builder()
                .pattern(entity.getPattern())
                .filename(entity.getFilename())
                .build();
    }

    public PathMappingEntity toEntity(PathMappingDTO dto) {
        if (dto == null) return null;
        return PathMappingEntity.builder()
                .pattern(dto.getPattern())
                .filename(dto.getFilename())
                .build();
    }

    public PathMappingModel toModel(PathMappingEntity entity) {
        if (entity == null) return null;
        return PathMappingModel.builder()
                .pattern(entity.getPattern())
                .filename(entity.getFilename())
                .build();
    }

    public PathMappingEntity toEntity(PathMappingModel model) {
        if (model == null) return null;
        return PathMappingEntity.builder()
                .pattern(model.getPattern())
                .filename(model.getFilename())
                .build();
    }

    // ─── Category Mappings ───────────────────────────────────────────────────

    public CategoryDTO toDTO(CategoryEntity entity) {
        if (entity == null) return null;
        List<PathMappingDTO> mappings = entity.getPathMappings() == null ? new ArrayList<>() :
                entity.getPathMappings().stream()
                        .map(this::toDTO)
                        .collect(Collectors.toList());

        return CategoryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .pathMappings(mappings)
                .priority(entity.getPriority())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public CategoryEntity toEntity(CategoryDTO dto) {
        if (dto == null) return null;
        List<PathMappingEntity> mappings = dto.getPathMappings() == null ? new ArrayList<>() :
                dto.getPathMappings().stream()
                        .map(this::toEntity)
                        .collect(Collectors.toList());

        return CategoryEntity.builder()
                .id(dto.getId())
                .name(dto.getName())
                .pathMappings(mappings)
                .priority(dto.getPriority())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    public CategoryModel toModel(CategoryEntity entity) {
        if (entity == null) return null;
        List<PathMappingModel> mappings = entity.getPathMappings() == null ? new ArrayList<>() :
                entity.getPathMappings().stream()
                        .map(this::toModel)
                        .collect(Collectors.toList());

        return CategoryModel.builder()
                .id(entity.getId())
                .name(entity.getName())
                .pathMappings(mappings)
                .priority(entity.getPriority())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public CategoryEntity toEntity(CategoryModel model) {
        if (model == null) return null;
        List<PathMappingEntity> mappings = model.getPathMappings() == null ? new ArrayList<>() :
                model.getPathMappings().stream()
                        .map(this::toEntity)
                        .collect(Collectors.toList());

        return CategoryEntity.builder()
                .id(model.getId())
                .name(model.getName())
                .pathMappings(mappings)
                .priority(model.getPriority())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .build();
    }

    // ─── HistoryEntry Mappings ────────────────────────────────────────────────

    public HistoryEntryDTO toDTO(HistoryEntryEntity entity) {
        if (entity == null) return null;
        return HistoryEntryDTO.builder()
                .locale(entity.getLocale())
                .modifiedBy(entity.getModifiedBy())
                .previousValue(entity.getPreviousValue())
                .newValue(entity.getNewValue())
                .action(entity.getAction())
                .timestamp(entity.getTimestamp())
                .build();
    }

    public HistoryEntryModel toModel(HistoryEntryEntity entity) {
        if (entity == null) return null;
        return HistoryEntryModel.builder()
                .locale(entity.getLocale())
                .modifiedBy(entity.getModifiedBy())
                .previousValue(entity.getPreviousValue())
                .newValue(entity.getNewValue())
                .action(entity.getAction() != null ? HistoryActionEnum.valueOf(entity.getAction().name().toUpperCase()) : null)
                .timestamp(entity.getTimestamp())
                .build();
    }

    public HistoryEntryEntity toEntity(HistoryEntryModel model) {
        if (model == null) return null;
        return HistoryEntryEntity.builder()
                .locale(model.getLocale())
                .modifiedBy(model.getModifiedBy())
                .previousValue(model.getPreviousValue())
                .newValue(model.getNewValue())
                .action(model.getAction() != null ? model.getAction() : null)
                .timestamp(model.getTimestamp())
                .build();
    }

    // ─── TranslationValue Mappings ───────────────────────────────────────────

    public TranslationValueDTO toDTO(TranslationValueEntity entity) {
        if (entity == null) return null;
        return TranslationValueDTO.builder()
                .translatedValue(entity.getTranslatedValue())
                .status(entity.getStatus())
                .lastModifiedBy(entity.getLastModifiedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public TranslationValueModel toModel(TranslationValueEntity entity) {
        if (entity == null) return null;
        return TranslationValueModel.builder()
                .translatedValue(entity.getTranslatedValue())
                .status(entity.getStatus())
                .lastModifiedBy(entity.getLastModifiedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public TranslationValueEntity toEntity(TranslationValueModel model) {
        if (model == null) return null;
        return TranslationValueEntity.builder()
                .translatedValue(model.getTranslatedValue())
                .status(model.getStatus())
                .lastModifiedBy(model.getLastModifiedBy())
                .updatedAt(model.getUpdatedAt())
                .build();
    }

    // ─── Translation / Aggregate Mappings ────────────────────────────────────

    public TranslationDocumentDTO toDTO(TranslationEntity entity) {
        if (entity == null) return null;

        Map<String, TranslationValueDTO> translationsDTO = new HashMap<>();
        if (entity.getTranslations() != null) {
            for (Map.Entry<String, TranslationValueEntity> entry : entity.getTranslations().entrySet()) {
                translationsDTO.put(entry.getKey(), toDTO(entry.getValue()));
            }
        }

        List<HistoryEntryDTO> historyDTO = entity.getHistory() == null ? new ArrayList<>() :
                entity.getHistory().stream()
                        .map(this::toDTO)
                        .collect(Collectors.toList());

        return TranslationDocumentDTO.builder()
                .id(entity.getId())
                .keyCode(entity.getKeyCode())
                .version(entity.getVersion())
                .category(entity.getCategory())
                .tags(entity.getTags() != null ? new ArrayList<>(entity.getTags()) : new ArrayList<>())
                .contextInfo(entity.getContextInfo())
                .baseValue(entity.getBaseValue())
                .translations(translationsDTO)
                .history(historyDTO)
                .priority(entity.getPriority())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public TranslationModel toModel(TranslationEntity entity) {
        if (entity == null) return null;

        Map<String, TranslationValueModel> translationsModel = new HashMap<>();
        if (entity.getTranslations() != null) {
            for (Map.Entry<String, TranslationValueEntity> entry : entity.getTranslations().entrySet()) {
                translationsModel.put(entry.getKey(), toModel(entry.getValue()));
            }
        }

        List<HistoryEntryModel> historyModel = entity.getHistory() == null ? new ArrayList<>() :
                entity.getHistory().stream()
                        .map(this::toModel)
                        .collect(Collectors.toList());

        return TranslationModel.builder()
                .id(entity.getId())
                .keyCode(entity.getKeyCode())
                .version(entity.getVersion())
                .category(entity.getCategory())
                .tags(entity.getTags() != null ? new ArrayList<>(entity.getTags()) : new ArrayList<>())
                .contextInfo(entity.getContextInfo())
                .baseValue(entity.getBaseValue())
                .translations(translationsModel)
                .history(historyModel)
                .priority(entity.getPriority())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public TranslationEntity toEntity(TranslationModel model) {
        if (model == null) return null;

        Map<String, TranslationValueEntity> translationsEntity = new HashMap<>();
        if (model.getTranslations() != null) {
            for (Map.Entry<String, TranslationValueModel> entry : model.getTranslations().entrySet()) {
                translationsEntity.put(entry.getKey(), toEntity(entry.getValue()));
            }
        }

        List<HistoryEntryEntity> historyEntity = model.getHistory() == null ? new ArrayList<>() :
                model.getHistory().stream()
                        .map(this::toEntity)
                        .collect(Collectors.toList());

        return TranslationEntity.builder()
                .id(model.getId())
                .keyCode(model.getKeyCode())
                .version(model.getVersion())
                .category(model.getCategory())
                .tags(model.getTags() != null ? new ArrayList<>(model.getTags()) : new ArrayList<>())
                .contextInfo(model.getContextInfo())
                .baseValue(model.getBaseValue())
                .translations(translationsEntity)
                .history(historyEntity)
                .priority(model.getPriority())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .build();
    }

    // ─── AppVersion Mappings ─────────────────────────────────────────────────

    public AppVersionModel toModel(AppVersionEntity entity) {
        if (entity == null) return null;
        return AppVersionModel.builder()
                .id(entity.getId())
                .version(entity.getVersion())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public AppVersionEntity toEntity(AppVersionModel model) {
        if (model == null) return null;
        return AppVersionEntity.builder()
                .id(model.getId())
                .version(model.getVersion())
                .active(model.isActive())
                .createdAt(model.getCreatedAt())
                .build();
    }

    // ─── User Mappings ───────────────────────────────────────────────────────

    public UserModel toModel(UserEntity entity) {
        if (entity == null) return null;
        try {
            return UserModel.builder()
                    .id(entity.getId())
                    .username(entity.getUsername())
                    .passwordHash(entity.getPasswordHash())
                    .roles(entity.getRoles() != null ? new ArrayList<>(entity.getRoles()) : new ArrayList<>())
                    .allowedLocales(entity.getAllowedLocales() != null ? new ArrayList<>(entity.getAllowedLocales()) : new ArrayList<>())
                    .build();
        } catch (BusinessException e) {
            throw new IllegalStateException("Database contains invalid user configuration", e);
        }
    }

    public UserEntity toEntity(UserModel model) {
        if (model == null) return null;
        return UserEntity.builder()
                .id(model.getId())
                .username(model.getUsername())
                .passwordHash(model.getPasswordHash())
                .roles(model.getRoles() != null ? new ArrayList<>(model.getRoles()) : new ArrayList<>())
                .allowedLocales(model.getAllowedLocales() != null ? new ArrayList<>(model.getAllowedLocales()) : new ArrayList<>())
                .build();
    }

    public UserDTO toDTO(UserEntity entity) {
        if (entity == null) return null;
        return new UserDTO(
                entity.getId(),
                entity.getUsername(),
                entity.getRoles(),
                entity.getAllowedLocales()
        );
    }
}
