package com.hieu.mapper;

import com.hieu.model.Author;
import com.hieu.payload.request.catalog.AuthorRequest;
import com.hieu.payload.response.catalog.AuthorResponse;

public class AuthorMapper {

    public static Author toEntity(AuthorRequest request) {
        if (request == null) {
            return null;
        }
        Author author = new Author();
        author.setName(request.getName());
        author.setActive(request.getActive() != null ? request.getActive() : Boolean.TRUE);
        author.setBio(request.getBio());
        return author;
    }

    public static AuthorResponse toResponse(Author author) {
        if (author == null) {
            return null;
        }
        AuthorResponse response = new AuthorResponse();
        response.setId(author.getId());
        response.setName(author.getName());
        response.setActive(author.getActive());
        response.setBio(author.getBio());
        return response;
    }

    public static Author updateEntity(Author author, AuthorRequest request) {
        if (author == null || request == null) {
            return author;
        }
        author.setName(request.getName());
        if (request.getActive() != null) {
            author.setActive(request.getActive());
        }
        author.setBio(request.getBio());
        return author;
    }
}