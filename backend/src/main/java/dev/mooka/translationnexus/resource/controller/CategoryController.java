package dev.mooka.translationnexus.resource.controller;

import dev.mooka.translationnexus.domain.Category;
import dev.mooka.translationnexus.exception.BusinessException;
import dev.mooka.translationnexus.service.TranslationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Endpoints for managing translation categories and key path patterns")
public class CategoryController {

    private final TranslationService translationService;

    @GetMapping
    @Operation(summary = "Get list of all categories")
    public ResponseEntity<List<Category>> list() {
        return ResponseEntity.ok(translationService.getAllCategories());
    }

    @PostMapping
    @Operation(summary = "Create a new category. MANAGER only.")
    public ResponseEntity<Category> create(@RequestBody Category category) throws BusinessException {
        return ResponseEntity.status(HttpStatus.CREATED).body(translationService.createCategory(category));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing category. MANAGER only.")
    public ResponseEntity<Category> update(@PathVariable String id, @RequestBody Category category) throws BusinessException {
        return ResponseEntity.ok(translationService.updateCategory(id, category));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category. MANAGER only.")
    public ResponseEntity<Void> delete(@PathVariable String id) throws BusinessException {
        translationService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
