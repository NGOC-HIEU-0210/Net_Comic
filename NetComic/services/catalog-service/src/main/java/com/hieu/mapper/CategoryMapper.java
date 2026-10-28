package com.hieu.mapper;

import com.hieu.model.Category;
import com.hieu.payload.request.catalog.CategoryRequest;
import com.hieu.payload.response.catalog.CategoryResponse;

public class CategoryMapper {

    public static Category toEntity(CategoryRequest request) {
        if (request == null) {
            return null;
        }
        Category category = new Category();
        category.setName(request.getName());
        category.setActive(request.getActive() != null ? request.getActive() : Boolean.TRUE);
        category.setDescription(request.getDescription());
        return category;
    }


    public static CategoryResponse toResponse(Category category) {
        if (category == null) {
            return null;
        }
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setActive(category.getActive());
        response.setDescrition(category.getDescription());
        return response;
    }


    public static Category updateEntity(Category category, CategoryRequest request) {
        if (category == null || request == null) {
            return category;
        }
        category.setName(request.getName());
        category.setActive(request.getActive());
        category.setDescription(request.getDescription());
        return category;
    }
}