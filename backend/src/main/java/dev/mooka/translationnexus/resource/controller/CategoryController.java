package dev.mooka.translationnexus.resource.controller;

import dev.mooka.translationnexus.domain.Category;
import dev.mooka.translationnexus.resource.dto.CategoryDTO;
import dev.mooka.translationnexus.service.MapperService;
import dev.mooka.translationnexus.service.CategoryService;
import dev.mooka.translationnexus.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Endpoints for managing translation categories and key path patterns")
public class CategoryController {

    private final CategoryService categoryService;
    private final MapperService mapperService;

    @GetMapping
    @Operation(summary = "Get list of all categories")
    public ResponseEntity<List<CategoryDTO>> list() {
        List<CategoryDTO> dtos = categoryService.getAllCategories().stream()
                .map(mapperService::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    @Operation(summary = "Create a new category. MANAGER only.")
    public ResponseEntity<CategoryDTO> create(@RequestBody CategoryDTO categoryDTO) throws BusinessException {
        Category category = mapperService.toEntity(categoryDTO);
        Category created = categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapperService.toDTO(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing category. MANAGER only.")
    public ResponseEntity<CategoryDTO> update(@PathVariable String id, @RequestBody CategoryDTO categoryDTO) throws BusinessException {
        Category category = mapperService.toEntity(categoryDTO);
        Category updated = categoryService.updateCategory(id, category);
        return ResponseEntity.ok(mapperService.toDTO(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category. MANAGER only.")
    public ResponseEntity<Void> delete(@PathVariable String id) throws BusinessException {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
