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
        List<LocaleEntity> locales = localeRepository.findAll().stream()
                .sorted(Comparator.comparing(LocaleEntity::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        List<String> localeIds = locales.stream().map(LocaleEntity::getId).toList();

        // Build CSV Headers: key, value_en, value_<locale1>, ...
        List<String> headersList = new ArrayList<>();
        headersList.add("key");
        headersList.add("value_en");
        for (String lid : localeIds) {
            headersList.add("value_" + lid);
        }
        String[] headers = headersList.toArray(new String[0]);

        List<CategoryEntity> categories = categoryRepository.findAll();
        List<TranslationEntity> allDocs = translationRepository.findAll();

        // Group rows by filename
        // Map<Filename, List<RowValues>>
        Map<String, List<List<String>>> groupedFiles = new LinkedHashMap<>();

        for (TranslationEntity doc : allDocs) {
            // Filter version
            if (version != null && !version.isBlank() && !version.equals(doc.getVersion())) {
                continue;
            }

            // Find matching category
            CategoryEntity matchingCategory = categories.stream()
                    .filter(c -> c.getName().equalsIgnoreCase(doc.getCategory()))
                    .findFirst()
                    .orElse(null);

            if (matchingCategory == null || matchingCategory.getPathMappings() == null) {
                continue;
            }

            // Find matching path mapping
            PathMappingEntity matchingMapping = matchingCategory.getPathMappings().stream()
                    .filter(pm -> CategoryValidationHelper.matchPath(doc.getKeyCode(), pm.getPattern()))
                    .findFirst()
                    .orElse(null);

            if (matchingMapping == null) {
                continue;
            }

            String filename = matchingMapping.getFilename();
            String alias = CategoryValidationHelper.extractAlias(doc.getKeyCode(), matchingMapping.getPattern());

            // Build CSV row
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

            groupedFiles.computeIfAbsent(filename, k -> new ArrayList<>()).add(row);
        }

        // If no files were grouped, ensure we write at least an empty zip or log
        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            for (Map.Entry<String, List<List<String>>> entry : groupedFiles.entrySet()) {
                String filename = entry.getKey();
                List<List<String>> rows = entry.getValue();

                zos.putNextEntry(new ZipEntry(filename));
                
                // Write CSV to the zip entry using UTF-8
                OutputStreamWriter writer = new OutputStreamWriter(zos, StandardCharsets.UTF_8);
                CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                        .setHeader(headers)
                        .build();

                try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
                    for (List<String> row : rows) {
                        csvPrinter.printRecord(row);
                    }
                    csvPrinter.flush();
                }
                zos.closeEntry();
            }
        }
    }
}
