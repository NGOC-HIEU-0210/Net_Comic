package com.hieu.mapper;

import java.util.Comparator;
import java.util.Set;

import com.hieu.enums.ComicStatus;
import com.hieu.model.Author;
import com.hieu.model.Category;
import com.hieu.model.Comic;
import com.hieu.payload.request.catalog.ComicRequest;
import com.hieu.payload.response.catalog.ComicResponse;

public class ComicMapper {

    public static Comic toEntity(ComicRequest request, Author author, Set<Category> categories) {
        if (request == null) {
            return null;
        }
        Comic comic = new Comic();
        comic.setTitle(request.getTitle() != null ? request.getTitle().trim() : null);
        comic.setDescription(request.getDescription());
        comic.setCoverUrl(request.getCoverUrl());
        comic.setStatus(request.getStatus() != null ? request.getStatus() : ComicStatus.DRAFT);
        comic.setAuthor(author);
        comic.setCategories(categories);
        return comic;
    }

    public static ComicResponse toResponse(Comic comic) {
        if (comic == null) {
            return null;
        }
        ComicResponse response = new ComicResponse();
        response.setId(comic.getId());
        response.setTitle(comic.getTitle());
        response.setDescription(comic.getDescription());
        response.setCoverUrl(comic.getCoverUrl());
        response.setStatus(comic.getStatus());

        if (comic.getAuthor() != null) {
            response.setAuthorId(comic.getAuthor().getId());
            response.setAuthorName(comic.getAuthor().getName());
        }

        response.setCategories(comic.getCategories().stream()
                .sorted(Comparator.comparing(Category::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(CategoryMapper::toResponse)
                .toList());
        return response;
    }

    public static Comic updateEntity(Comic comic, ComicRequest request, Author author, Set<Category> categories) {
        if (comic == null || request == null) {
            return comic;
        }
        comic.setTitle(request.getTitle() != null ? request.getTitle().trim() : null);
        comic.setDescription(request.getDescription());
        comic.setCoverUrl(request.getCoverUrl());
        comic.setAuthor(author);
        comic.setCategories(categories);
        if (request.getStatus() != null) {
            comic.setStatus(request.getStatus());
        }
        return comic;
    }
}
