package dev.mooka.translationnexus.service;

import dev.mooka.translationnexus.domain.*;
import dev.mooka.translationnexus.resource.dto.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MapperService {

    public LocaleDTO toDTO(Locale locale) {
        if (locale == null) return null;
        return LocaleDTO.builder()
                .id(locale.getId())
                .name(locale.getName())
                .googleCode(locale.getGoogleCode())
                .sortOrder(locale.getSortOrder())
                .build();
    }

    public Locale toEntity(LocaleDTO dto) {
        if (dto == null) return null;
        return Locale.builder()
                .id(dto.getId())
                .name(dto.getName())
                .googleCode(dto.getGoogleCode())
                .sortOrder(dto.getSortOrder())
                .build();
    }

    public PathMappingDTO toDTO(PathMapping mapping) {
        if (mapping == null) return null;
        return PathMappingDTO.builder()
                .pattern(mapping.getPattern())
                .filename(mapping.getFilename())
                .build();
    }

    public PathMapping toEntity(PathMappingDTO dto) {
        if (dto == null) return null;
        return PathMapping.builder()
                .pattern(dto.getPattern())
                .filename(dto.getFilename())
                .build();
    }

    public CategoryDTO toDTO(Category category) {
        if (category == null) return null;
        List<PathMappingDTO> mappings = category.getPathMappings() == null ? new ArrayList<>() :
                category.getPathMappings().stream()
                        .map(this::toDTO)
                        .collect(Collectors.toList());

        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .pathMappings(mappings)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    public Category toEntity(CategoryDTO dto) {
        if (dto == null) return null;
        List<PathMapping> mappings = dto.getPathMappings() == null ? new ArrayList<>() :
                dto.getPathMappings().stream()
                        .map(this::toEntity)
                        .collect(Collectors.toList());

        return Category.builder()
                .id(dto.getId())
                .name(dto.getName())
                .pathMappings(mappings)
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    public HistoryEntryDTO toDTO(HistoryEntry entry) {
        if (entry == null) return null;
        return HistoryEntryDTO.builder()
                .locale(entry.getLocale())
                .modifiedBy(entry.getModifiedBy())
                .previousValue(entry.getPreviousValue())
                .newValue(entry.getNewValue())
                .action(entry.getAction())
                .timestamp(entry.getTimestamp())
                .build();
    }

    public TranslationValueDTO toDTO(TranslationValue value) {
        if (value == null) return null;
        return TranslationValueDTO.builder()
                .translatedValue(value.getTranslatedValue())
                .status(value.getStatus())
                .lastModifiedBy(value.getLastModifiedBy())
                .updatedAt(value.getUpdatedAt())
                .build();
    }

    public TranslationDocumentDTO toDTO(TranslationDocument doc) {
        if (doc == null) return null;
        
        Map<String, TranslationValueDTO> translationsDTO = new HashMap<>();
        if (doc.getTranslations() != null) {
            for (Map.Entry<String, TranslationValue> entry : doc.getTranslations().entrySet()) {
                translationsDTO.put(entry.getKey(), toDTO(entry.getValue()));
            }
        }

        List<HistoryEntryDTO> historyDTO = doc.getHistory() == null ? new ArrayList<>() :
                doc.getHistory().stream()
                        .map(this::toDTO)
                        .collect(Collectors.toList());

        return TranslationDocumentDTO.builder()
                .id(doc.getId())
                .keyCode(doc.getKeyCode())
                .version(doc.getVersion())
                .category(doc.getCategory())
                .tags(doc.getTags() != null ? new ArrayList<>(doc.getTags()) : new ArrayList<>())
                .contextInfo(doc.getContextInfo())
                .baseValue(doc.getBaseValue())
                .translations(translationsDTO)
                .history(historyDTO)
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }
}
