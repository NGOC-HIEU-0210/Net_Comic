package com.hieu.service.impl;

import com.hieu.payload.request.catalog.AuthorRequest;
import com.hieu.payload.response.catalog.AuthorResponse;
import com.hieu.repository.AuthorRepository;
import com.hieu.service.AuthorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorServiceImpl(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Override
    public AuthorResponse createAuthor(AuthorRequest request) throws Exception {
        return null;
    }

    @Override
    public AuthorResponse updateAuthor(Long id, AuthorRequest request) throws Exception {
        return null;
    }

    @Override
    public void deleteAuthor(Long id) throws Exception {

    }

    @Override
    public AuthorResponse getAuthorById(Long id) throws Exception {
        return null;
    }

    @Override
    public Page<AuthorResponse> getAllAuthors(Pageable pageable) {
        return null;
    }

    @Override
    public Page<AuthorResponse> searchAuthors(String keyword, Pageable pageable) {
        return null;
    }

    @Override
    public List<AuthorResponse> getAuthorByActiveTrue() {
        return List.of();
    }

    @Override
    public List<AuthorResponse> searchAuthorByActiveTrue(String keyword) {
        return List.of();
    }
}
