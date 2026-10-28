package com.hieu.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hieu.payload.request.catalog.CategoryRequest;
import com.hieu.payload.response.catalog.CategoryResponse;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest request) throws Exception;

    CategoryResponse updateCategory(Long id, CategoryRequest request) throws Exception;

    void deleteCategory(Long id) throws Exception;

    CategoryResponse getCategoryById(Long id) throws Exception;

    Page<CategoryResponse> getAllCategories(Pageable pageable);

    Page<CategoryResponse> searchCategories(String keyword,Pageable pageable);

    List<CategoryResponse> searchCategoriesByActiveTrue(String keyword);


    List<CategoryResponse> getCategoryByActiveTrue();
}