package com.hieu.controller;

import com.hieu.payload.request.catalog.ComicAssignmentRequest;
import com.hieu.payload.request.catalog.ReassignStaffRequest;
import com.hieu.payload.response.ApiResponse;
import com.hieu.payload.response.catalog.ComicAssignmentResponse;
import com.hieu.service.ComicAssignmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/version-1/comic-assignments")
public class ComicAssignmentController {

    private final ComicAssignmentService assignmentService;

    public ComicAssignmentController(ComicAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    // tên cho phép client sắp xếp -> đường dẫn thuộc tính trong entity
    private static final Map<String, String> SORTABLE_FIELDS =
            Map.of("comicId", "id.comicId", "staffId", "id.staffId");
    private static final int MAX_PAGE_SIZE = 100;

    @PostMapping
    public ResponseEntity<ComicAssignmentResponse> createComicAssignment(
            @Valid @RequestBody ComicAssignmentRequest request) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(assignmentService.createComicAssignment(request));
    }

    @PutMapping("/{comicId}/{staffId}")
    public ResponseEntity<ComicAssignmentResponse> updateComicAssignment(
            @PathVariable Long comicId,
            @PathVariable Long staffId,
            @Valid @RequestBody ReassignStaffRequest request) throws Exception {
        return ResponseEntity.ok(assignmentService.updateComicAssignment(comicId, staffId, request));
    }

    @DeleteMapping("/{comicId}/{staffId}")
    public ResponseEntity<ApiResponse> deleteComicAssignment(
            @PathVariable Long comicId,
            @PathVariable Long staffId) throws Exception {
        assignmentService.deleteComicAssignment(comicId, staffId);
        return ResponseEntity.ok(new ApiResponse("Xóa thành công"));
    }

    @GetMapping("/{comicId}/{staffId}")
    public ResponseEntity<ComicAssignmentResponse> getComicAssignment(
            @PathVariable Long comicId,
            @PathVariable Long staffId) throws Exception {
        return ResponseEntity.ok(assignmentService.getComicAssignment(comicId, staffId));
    }

    @GetMapping
    public ResponseEntity<Page<ComicAssignmentResponse>> getAllComicAssignments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "comicId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return ResponseEntity.ok(assignmentService.getAllComicAssignments(
                buildPageable(page, size, sortBy, sortDirection, "comicId")));
    }

    @GetMapping("/by-comic/{comicId}")
    public ResponseEntity<Page<ComicAssignmentResponse>> getAssignmentsByComic(
            @PathVariable Long comicId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "staffId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) throws Exception {
        return ResponseEntity.ok(assignmentService.getAssignmentsByComic(
                comicId, buildPageable(page, size, sortBy, sortDirection, "staffId")));
    }

    @GetMapping("/by-staff/{staffId}")
    public ResponseEntity<Page<ComicAssignmentResponse>> getAssignmentsByStaff(
            @PathVariable Long staffId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "comicId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByStaff(
                staffId, buildPageable(page, size, sortBy, sortDirection, "comicId")));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDirection, String defaultSort) {
        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        String property = SORTABLE_FIELDS.getOrDefault(sortBy, SORTABLE_FIELDS.get(defaultSort));
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return PageRequest.of(page, size, Sort.by(direction, property));
    }
}
