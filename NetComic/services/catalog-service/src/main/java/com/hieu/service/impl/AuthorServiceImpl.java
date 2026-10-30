package com.hieu.service.impl;

import com.hieu.mapper.AuthorMapper;
import com.hieu.model.Author;
import com.hieu.payload.request.catalog.AuthorRequest;
import com.hieu.payload.response.catalog.AuthorResponse;
import com.hieu.repository.AuthorRepository;
import com.hieu.service.AuthorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorServiceImpl(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthorResponse createAuthor(AuthorRequest request) throws Exception {

        Author author = AuthorMapper.toEntity(request);
        Author saved = authorRepository.save(author);
        return AuthorMapper.toResponse(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthorResponse updateAuthor(Long id, AuthorRequest request) throws Exception {
        Author author = authorRepository.findById(id).orElseThrow(
                () -> new Exception("Không tìm thấy tác giả với id = " + id)
        );



        AuthorMapper.updateEntity(author, request);
        Author saved = authorRepository.save(author);
        return AuthorMapper.toResponse(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAuthor(Long id) throws Exception {
        Author author = authorRepository.findById(id).orElseThrow(
                () -> new Exception("Không tìm thấy tác giả với id = " + id)
        );
        author.setActive(Boolean.FALSE);
        authorRepository.save(author);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthorResponse getAuthorById(Long id) throws Exception {
        Author author = authorRepository.findById(id).orElseThrow(
                () -> new Exception("Không tìm thấy tác giả với id = " + id)
        );
        return AuthorMapper.toResponse(author);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuthorResponse> getAllAuthors(Pageable pageable) {
        return authorRepository.findAll(pageable)
                .map(AuthorMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuthorResponse> searchAuthors(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllAuthors(pageable);
        }
        return authorRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable)
                .map(AuthorMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuthorResponse> getAuthorByActiveTrue() {
        return authorRepository.findByActiveTrue()
                .stream()
                .map(AuthorMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuthorResponse> searchAuthorByActiveTrue(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAuthorByActiveTrue();
        }
        return authorRepository.findByNameContainingIgnoreCaseAndActiveTrue(keyword.trim())
                .stream()
                .map(AuthorMapper::toResponse)
                .toList();
    }
}