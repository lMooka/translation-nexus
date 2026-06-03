package dev.mooka.translationnexus.service;

import dev.mooka.translationnexus.core.CategoryValidationHelper;
import dev.mooka.translationnexus.domain.Category;
import dev.mooka.translationnexus.domain.PathMapping;
import dev.mooka.translationnexus.exception.BusinessException;
import dev.mooka.translationnexus.exception.impl.CategoryNotFoundException;
import dev.mooka.translationnexus.exception.impl.GenericBusinessException;
import dev.mooka.translationnexus.exception.impl.InvalidKeyPathException;
import dev.mooka.translationnexus.repository.CategoryRepository;
import dev.mooka.translationnexus.repository.TranslationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TranslationRepository translationRepository;

    public void validateCategoryAndPath(String categoryName, String keyCode) throws BusinessException {
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

        log.info("Category created: {}", cleanName);
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

        log.info("Category updated: {}", id);
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
        log.info("Category deleted: {}", id);
    }
}
