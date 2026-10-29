package com.hieu.controller;

import com.hieu.payload.request.catalog.AuthorRequest;
import com.hieu.payload.response.ApiResponse;
import com.hieu.payload.response.catalog.AuthorResponse;
import com.hieu.service.AuthorService;
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

@RestController
@RequestMapping("/api/version-1/authors")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "name", "active");
    private static final int MAX_PAGE_SIZE = 100;

    @PostMapping
    public ResponseEntity<AuthorResponse> createAuthor(@Valid @RequestBody AuthorRequest request) throws Exception {
        AuthorResponse authorResponse = authorService.createAuthor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(authorResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuthorResponse> updateAuthor(
            @PathVariable Long id,
            @Valid @RequestBody AuthorRequest request) throws Exception {
        AuthorResponse authorResponse = authorService.updateAuthor(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(authorResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteAuthor(@PathVariable Long id) throws Exception {
        authorService.deleteAuthor(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("Xóa thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponse> getAuthorById(@PathVariable Long id) throws Exception {
        AuthorResponse response = authorService.getAuthorById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<AuthorResponse>> getAllAuthors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        if (!SORTABLE_FIELDS.contains(sortBy)) {
            sortBy = "name";
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return ResponseEntity.ok(authorService.getAllAuthors(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<AuthorResponse>> searchAuthors(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(authorService.searchAuthors(keyword, pageable));
    }

    @GetMapping("/active")
    public ResponseEntity<List<AuthorResponse>> getActiveAuthors() {
        return ResponseEntity.ok(authorService.getAuthorByActiveTrue());
    }

    @GetMapping("/search-active")
    public ResponseEntity<List<AuthorResponse>> searchAuthorsActiveTrue(
            @RequestParam(required = false, defaultValue = "") String keyword) {
        return ResponseEntity.ok(authorService.searchAuthorByActiveTrue(keyword));
    }
}