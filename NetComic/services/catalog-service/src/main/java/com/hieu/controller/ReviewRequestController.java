package com.hieu.controller;

import com.hieu.enums.ReviewStatus;
import com.hieu.payload.request.catalog.ReviewApproveRequest;
import com.hieu.payload.request.catalog.ReviewRejectRequest;
import com.hieu.payload.request.catalog.ReviewSubmitRequest;
import com.hieu.payload.response.ApiResponse;
import com.hieu.payload.response.catalog.ReviewRequestResponse;
import com.hieu.service.ReviewRequestService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "ReviewRequests", description = "Quản lý yêu cầu duyệt (review request) của chương")
@RestController
@RequestMapping("/api/version-1/review-requests")
public class ReviewRequestController {

    private final ReviewRequestService reviewRequestService;

    public ReviewRequestController(ReviewRequestService reviewRequestService) {
        this.reviewRequestService = reviewRequestService;
    }

    // tên client gửi -> property trên ReviewRequest. Không cho sort theo field lạ
    private static final Map<String, String> SORTABLE_FIELDS = Map.of(
            "id", "id",
            "status", "status",
            "submittedBy", "submittedBy",
            "reviewedBy", "reviewedBy"
    );
    private static final int MAX_PAGE_SIZE = 100;

    @Operation(summary = "Gửi duyệt chương", description = "Staff gửi duyệt: DRAFT -> PENDING_REVIEW, tạo ReviewRequest mới.")
    @PostMapping
    public ResponseEntity<ReviewRequestResponse> submit(
            @Valid @RequestBody ReviewSubmitRequest request) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewRequestService.submit(request));
    }

    @Operation(summary = "Lấy yêu cầu duyệt theo id", description = "Lấy chi tiết một yêu cầu duyệt.")
    @GetMapping("/{id}")
    public ResponseEntity<ReviewRequestResponse> getById(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(reviewRequestService.getById(id));
    }

    @Operation(summary = "Lấy danh sách yêu cầu duyệt", description = "Lấy danh sách yêu cầu duyệt, lọc theo status (null = tất cả), có phân trang.")
    @GetMapping
    public ResponseEntity<Page<ReviewRequestResponse>> getAll(
            @RequestParam(required = false) ReviewStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return ResponseEntity.ok(reviewRequestService.getAll(
                status, buildPageable(page, size, sortBy, sortDirection)));
    }

    @Operation(summary = "Lấy danh sách chờ duyệt", description = "Lấy danh sách các yêu cầu duyệt đang ở trạng thái PENDING.")
    @GetMapping("/pending")
    public ResponseEntity<Page<ReviewRequestResponse>> getPending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return ResponseEntity.ok(reviewRequestService.getPending(
                buildPageable(page, size, sortBy, sortDirection)));
    }

    @Operation(summary = "Lấy danh sách duyệt theo chương", description = "Lấy danh sách yêu cầu duyệt của một chương.")
    @GetMapping("/by-chapter/{chapterId}")
    public ResponseEntity<Page<ReviewRequestResponse>> getByChapter(
            @PathVariable Long chapterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) throws Exception {
        return ResponseEntity.ok(reviewRequestService.getByChapter(
                chapterId, buildPageable(page, size, sortBy, sortDirection)));
    }

    @Operation(summary = "Lấy danh sách duyệt theo nhân viên", description = "Lấy danh sách yêu cầu duyệt mà một nhân viên đã gửi.")
    @GetMapping("/by-staff/{staffId}")
    public ResponseEntity<Page<ReviewRequestResponse>> getByStaff(
            @PathVariable Long staffId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return ResponseEntity.ok(reviewRequestService.getByStaff(
                staffId, buildPageable(page, size, sortBy, sortDirection)));
    }

    @Operation(summary = "Duyệt yêu cầu", description = "Admin duyệt: PENDING_REVIEW -> SCHEDULED (nếu có publishAt trong tương lai) hoặc PUBLISHED.")
    @PostMapping("/{id}/approve")
    public ResponseEntity<ReviewRequestResponse> approve(
            @PathVariable Long id,
            @Valid @RequestBody ReviewApproveRequest request) throws Exception {
        return ResponseEntity.ok(reviewRequestService.approve(id, request));
    }

    @Operation(summary = "Từ chối yêu cầu", description = "Admin từ chối: PENDING_REVIEW -> DRAFT, bắt buộc phải có lý do.")
    @PostMapping("/{id}/reject")
    public ResponseEntity<ReviewRequestResponse> reject(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRejectRequest request) throws Exception {
        return ResponseEntity.ok(reviewRequestService.reject(id, request));
    }

    @Operation(summary = "Hủy yêu cầu duyệt", description = "Staff hủy phiếu của chính mình khi còn PENDING.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> cancel(
            @PathVariable Long id,
            @RequestParam Long staffId) throws Exception {
        reviewRequestService.cancel(id, staffId);
        return ResponseEntity.ok(new ApiResponse("Hủy yêu cầu duyệt thành công"));
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
