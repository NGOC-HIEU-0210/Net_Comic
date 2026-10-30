package com.hieu.controller;

import com.hieu.enums.ChapterStatus;
import com.hieu.payload.request.catalog.ChapterRequest;
import com.hieu.payload.response.ApiResponse;
import com.hieu.payload.response.catalog.ChapterResponse;
import com.hieu.service.ChapterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import java.util.Map;

@Tag(name = "Chapters", description = "Quản lý chương (chapter) của các truyện")
@RestController
@RequestMapping("/api/version-1/chapters")
public class ChapterController {

    private final ChapterService chapterService;

    public ChapterController(ChapterService chapterService) {
        this.chapterService = chapterService;
    }

    // tên cho phép client sắp xếp -> property trên entity
    private static final Map<String, String> SORTABLE_FIELDS = Map.of(
            "id", "id",
            "chapterNo", "chapterNo",
            "title", "title",
            "status", "status"
    );
    private static final int MAX_PAGE_SIZE = 100;

    @Operation(summary = "Tạo chương mới", description = "Tạo chương mới cho một truyện. Mỗi truyện không được có 2 chương cùng số.")
    @PostMapping
    public ResponseEntity<ChapterResponse> createChapter(@Valid @RequestBody ChapterRequest request) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                chapterService.createChapter(request.getComicId(), request));
    }

    @Operation(summary = "Cập nhật chương", description = "Cập nhật toàn bộ thông tin chương của chương đang có id.")
    @PutMapping("/{id}")
    public ResponseEntity<ChapterResponse> updateChapter(
            @PathVariable Long id,
            @Valid @RequestBody ChapterRequest request) throws Exception {
        return ResponseEntity.ok(chapterService.updateChapter(id, request));
    }

    @Operation(summary = "Xóa chương", description = "Xóa chương theo id.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteChapter(@PathVariable Long id) throws Exception {
        chapterService.deleteChapter(id);
        return ResponseEntity.ok(new ApiResponse("Xóa chương thành công"));
    }

    @Operation(summary = "Lấy chương theo id", description = "Lấy chi tiết một chương.")
    @GetMapping("/{id}")
    public ResponseEntity<ChapterResponse> getChapterById(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(chapterService.getChapterById(id));
    }

    @Operation(summary = "Lấy danh sách chương theo truyện", description = "Lấy danh sách chương của một truyện, lọc theo trạng thái (null = tất cả), có phân trang.")
    @GetMapping
    public ResponseEntity<Page<ChapterResponse>> getChaptersByComic(
            @Parameter(description = "ID truyện", required = true) @RequestParam Long comicId,
            @Parameter(description = "Lọc theo trạng thái (null = tất cả)") @RequestParam(required = false) ChapterStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) throws Exception {
        return ResponseEntity.ok(chapterService.getChaptersByComic(
                comicId, status, buildPageable(page, size, sortBy, sortDirection)));
    }

    @Operation(summary = "Gửi chương đi duyệt", description = "Staff gửi duyệt: DRAFT -> PENDING_REVIEW, tạo ReviewRequest mới.")
    @PostMapping("/{chapterId}/submit")
    public ResponseEntity<ChapterResponse> submitForReview(
            @Parameter(description = "ID chương") @PathVariable Long chapterId,
            @Parameter(description = "ID staff gửi duyệt") @RequestParam Long staffId) throws Exception {
        return ResponseEntity.ok(chapterService.submitForReview(chapterId, staffId));
    }

    @Operation(summary = "Duyệt chương", description = "Admin duyệt: PENDING_REVIEW -> SCHEDULED (nếu có publishAt trong tương lai) hoặc PUBLISHED.")
    @PostMapping("/{chapterId}/approve")
    public ResponseEntity<ChapterResponse> approveChapter(
            @Parameter(description = "ID chương") @PathVariable Long chapterId,
            @Parameter(description = "ID admin duyệt") @RequestParam Long adminId) throws Exception {
        return ResponseEntity.ok(chapterService.approveChapter(chapterId, adminId));
    }

    @Operation(summary = "Từ chối chương", description = "Admin từ chối: PENDING_REVIEW -> DRAFT, bắt buộc phải có lý do.")
    @PostMapping("/{chapterId}/reject")
    public ResponseEntity<ChapterResponse> rejectChapter(
            @Parameter(description = "ID chương") @PathVariable Long chapterId,
            @Parameter(description = "ID admin từ chối") @RequestParam Long adminId,
            @Parameter(description = "Lý do từ chối") @RequestParam String reason) throws Exception {
        return ResponseEntity.ok(chapterService.rejectChapter(chapterId, adminId, reason));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDirection) {
        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        String property = SORTABLE_FIELDS.getOrDefault(sortBy, "id");
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return PageRequest.of(page, size, Sort.by(direction, property));
    }
}
