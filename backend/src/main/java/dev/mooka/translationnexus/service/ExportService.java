package dev.mooka.translationnexus.service;

import dev.mooka.translationnexus.domain.entity.CategoryEntity;
import dev.mooka.translationnexus.domain.entity.LocaleEntity;
import dev.mooka.translationnexus.domain.entity.PathMappingEntity;
import dev.mooka.translationnexus.domain.entity.TranslationEntity;
import dev.mooka.translationnexus.domain.entity.TranslationValueEntity;
import dev.mooka.translationnexus.domain.enums.TranslationStatusEnum;
import dev.mooka.translationnexus.repository.CategoryRepository;
import dev.mooka.translationnexus.repository.LocaleRepository;
import dev.mooka.translationnexus.repository.TranslationRepository;
import dev.mooka.translationnexus.shared.CategoryValidationHelper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final TranslationRepository translationRepository;
    private final LocaleRepository localeRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Exports APPROVED translations grouped by target filename (from path mapping)
     * as separate CSV files inside a ZIP archive.
     */
    public void exportZip(String version, OutputStream outputStream) throws IOException {
        // find all locations by version
        List<LocaleEntity> locales = localeRepository.findAll().stream()
                .sorted(Comparator.comparing(LocaleEntity::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        List<String> localeIds = locales.stream().map(LocaleEntity::getId).toList();

        List<String> headersList = new ArrayList<>();
        headersList.add("key");
        headersList.add("en"); // English base value
        for (String lid : localeIds) {
            headersList.add(lid);
        }
        String[] headers = headersList.toArray(new String[0]);

        List<CategoryEntity> categories = categoryRepository.findAll();
        List<TranslationEntity> allDocs = translationRepository.findAllByVersion(version);

        // Map containing the final CSV rows for each filename
        Map<String, List<List<String>>> groupedFiles = new LinkedHashMap<>();

        // Loop over each category
        for (CategoryEntity category : categories) {
            if (category.getPathMappings() == null) {
                continue;
            }

            // For each category, find all path mappings
            for (PathMappingEntity pm : category.getPathMappings()) {
                String filename = pm.getFilename();
                if (filename == null || filename.isBlank()) {
                    continue;
                }

                List<List<String>> rows = new ArrayList<>();

                // Find translations for this path mapping
                for (TranslationEntity doc : allDocs) {
                    if (doc.getTags() != null && doc.getTags().stream().anyMatch(t -> t.equalsIgnoreCase("Obsolete"))) {
                        continue;
                    }
                    if (category.getName().equalsIgnoreCase(doc.getCategory())) {
                        if (CategoryValidationHelper.matchPath(doc.getKeyCode(), pm.getPattern())) {
                            String alias = CategoryValidationHelper.extractAlias(doc.getKeyCode(), pm.getPattern());
                            List<String> row = new ArrayList<>();
                            row.add(alias);
                            row.add(doc.getBaseValue() != null ? doc.getBaseValue() : "");

                            for (String lid : localeIds) {
                                TranslationValueEntity tv = doc.getTranslations().get(lid);
                                if (tv != null && tv.getStatus() == TranslationStatusEnum.APPROVED) {
                                    row.add(tv.getTranslatedValue() != null ? tv.getTranslatedValue() : "");
                                } else {
                                    row.add("");
                                }
                            }
                            rows.add(row);
                        }
                    }
                }

                groupedFiles.put(filename, rows);
            }
        }

        // create a zip archive with all csv files
        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            for (Map.Entry<String, List<List<String>>> entry : groupedFiles.entrySet()) {
                String filename = entry.getKey();
                List<List<String>> rows = entry.getValue();

                zos.putNextEntry(new ZipEntry(filename));

                OutputStreamWriter writer = new OutputStreamWriter(zos, StandardCharsets.UTF_8);
                CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                        .setHeader(headers)
                        .build();

                CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);
                for (List<String> row : rows) {
                    csvPrinter.printRecord(row);
                }
                csvPrinter.flush();

                zos.closeEntry();
            }

        }
    }
}
