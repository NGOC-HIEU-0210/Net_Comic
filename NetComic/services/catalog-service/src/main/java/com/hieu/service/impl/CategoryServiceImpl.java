package com.hieu.service.impl;

import com.hieu.mapper.CategoryMapper;
import com.hieu.model.Category;
import com.hieu.payload.request.catalog.CategoryRequest;
import com.hieu.payload.response.catalog.CategoryResponse;
import com.hieu.repository.CategoryRepository;
import com.hieu.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) throws Exception {
        if (categoryRepository.existsByName(request.getName())) {
            throw new Exception("Tên danh mục đã tồn tại");
        }
        Category category = CategoryMapper.toEntity(request);
        Category saved = categoryRepository.save(category);
        return CategoryMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) throws Exception {
        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new Exception("Không tìm thấy danh mục với id = " + id)
        );

        if (categoryRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new Exception("Tên danh mục đã tồn tại");
        }

        CategoryMapper.updateEntity(category, request);
        Category saved = categoryRepository.save(category);
        return CategoryMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) throws Exception {
        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new Exception("Không tìm thấy danh mục với id = " + id)
        );
        category.setActive(Boolean.FALSE);
        categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) throws Exception {
        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new Exception("Không tìm thấy danh mục với id = " + id)
        );
        return CategoryMapper.toResponse(category);
    }



    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(CategoryMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> searchCategories(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCategories(pageable);
        }
        return categoryRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable)
                .map(CategoryMapper::toResponse);
    }

    @Override
    public Page<CategoryResponse> getCategoryByActiveTrue(Pageable pageable) {
        return categoryRepository.findByActiveTrue(pageable).map(CategoryMapper::toResponse);
    }
}
