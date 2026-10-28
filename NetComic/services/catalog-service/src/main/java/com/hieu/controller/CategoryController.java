package com.hieu.controller;

import com.hieu.payload.request.catalog.CategoryRequest;
import com.hieu.payload.response.ApiResponse;
import com.hieu.payload.response.catalog.CategoryResponse;
import com.hieu.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static org.hibernate.dialect.SybaseASEDialect.MAX_PAGE_SIZE;

@RestController
@RequestMapping("/api/version-1/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "name", "active");
    private static final int MAX_PAGE_SIZE = 100;

    @PostMapping

    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) throws Exception {
        CategoryResponse categoryResponse = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) throws Exception {
        CategoryResponse categoryResponse = categoryService.updateCategory(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(categoryResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Long id) throws Exception {
        categoryService.deleteCategory(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("Xóa thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) throws Exception {
        CategoryResponse response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {


        page = Math.max(0, page);


        size = Math.max(1, Math.min(size, 100));


        List<String> allowedSortFields = List.of("id", "name", "active");

        if (!allowedSortFields.contains(sortBy)) {
            sortBy = "name";
        }


        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy)
        );

        return ResponseEntity.ok(categoryService.getAllCategories(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CategoryResponse>> searchCategories(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(categoryService.searchCategories(keyword, pageable));
    }

    @GetMapping("/active")
    public ResponseEntity<List<CategoryResponse>> getActiveCategories() {
        return ResponseEntity.ok(categoryService.getCategoryByActiveTrue());
    }

    @GetMapping("/search-active")
    public ResponseEntity<List<CategoryResponse>> searchCategoriesActiveTrue(
            @RequestParam(required = false, defaultValue = "") String keyword) {
        return ResponseEntity.ok(categoryService.searchCategoriesByActiveTrue(keyword));
    }

}
