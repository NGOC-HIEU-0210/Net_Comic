package com.hieu.controller;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hieu.enums.ComicStatus;
import com.hieu.payload.request.catalog.ComicRequest;
import com.hieu.payload.response.ApiResponse;
import com.hieu.payload.response.catalog.ComicResponse;
import com.hieu.service.ComicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Comics", description = "Quản lý truyện (comic) của các tác giả")
@RestController
@RequestMapping("/api/version-1/comics")
public class ComicController {

    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "title", "status");
    private static final int MAX_PAGE_SIZE = 100;

    private final ComicService comicService;

    public ComicController(ComicService comicService) {
        this.comicService = comicService;
    }

    @Operation(summary = "Tạo truyện mới", description = "Tạo truyện mới cho một tác giả. Mỗi tác giả không được có 2 truyện cùng tên.")
    @PostMapping
    public ResponseEntity<ComicResponse> createComic(@Valid @RequestBody ComicRequest request) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(comicService.createComic(request));
    }

    @Operation(summary = "Cập nhật truyện", description = "Cập nhật toàn bộ thông tin truyện theo id.")
    @PutMapping("/{id}")
    public ResponseEntity<ComicResponse> updateComic(
            @PathVariable Long id,
            @Valid @RequestBody ComicRequest request) throws Exception {
        return ResponseEntity.ok(comicService.updateComic(id, request));
    }

    @Operation(summary = "Xóa truyện", description = "Xóa truyện theo id (soft delete: chuyển truyện sang trạng thái HIDDEN).")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteComic(@PathVariable Long id) throws Exception {
        comicService.deleteComic(id);
        return ResponseEntity.ok(new ApiResponse("Đã ẩn truyện"));
    }

    @Operation(summary = "Lấy truyện theo id", description = "Lấy chi tiết một truyện.")
    @GetMapping("/{id}")
    public ResponseEntity<ComicResponse> getComicById(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(comicService.getComicById(id));
    }

    @Operation(summary = "Lấy danh sách truyện", description = "Lấy danh sách truyện có phân trang, lọc theo từ khóa, trạng thái, tác giả và thể loại.")
    @GetMapping
    public ResponseEntity<Page<ComicResponse>> getComics(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ComicStatus status,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        if (!SORTABLE_FIELDS.contains(sortBy)) {
            sortBy = "id";
        }

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return ResponseEntity.ok(comicService.searchComics(keyword, status, authorId, categoryId, pageable));
    }
}
