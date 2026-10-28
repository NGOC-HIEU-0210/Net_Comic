package com.hieu.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hieu.payload.request.catalog.AuthorRequest;
import com.hieu.payload.response.catalog.AuthorResponse;

import java.util.List;

public interface AuthorService {

    AuthorResponse createAuthor(AuthorRequest request) throws Exception;

    AuthorResponse updateAuthor(Long id, AuthorRequest request) throws Exception;

    void deleteAuthor(Long id) throws Exception;

    AuthorResponse getAuthorById(Long id) throws Exception;

    Page<AuthorResponse> getAllAuthors(Pageable pageable);

    Page<AuthorResponse> searchAuthors(String keyword, Pageable pageable);

    List<AuthorResponse> getAuthorByActiveTrue();
    List<AuthorResponse> searchAuthorByActiveTrue(String keyword);
}