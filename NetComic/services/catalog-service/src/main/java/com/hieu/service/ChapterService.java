package com.hieu.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hieu.enums.ChapterStatus;
import com.hieu.payload.request.catalog.ChapterRequest;
import com.hieu.payload.response.catalog.ChapterResponse;
import io.swagger.v3.oas.annotations.Operation;

public interface ChapterService {


    @Operation(summary = "Tạo chương mới", description = "Tạo chương mới cho một truyện. Mỗi truyện không được có 2 chương cùng số.")
    ChapterResponse createChapter(Long comicId, ChapterRequest request) throws Exception;

    @Operation(summary = "Cập nhật chương", description = "Cập nhật toàn bộ thông tin chương của chương đang có id.")
    ChapterResponse updateChapter(Long id, ChapterRequest request) throws Exception;


    @Operation(summary = "Xóa chương", description = "Xóa chương theo id.")
    void deleteChapter(Long id) throws Exception;

    @Operation(summary = "Lấy chương theo id", description = "Lấy chi tiết một chương.")
    ChapterResponse getChapterById(Long id) throws Exception;

    // Danh sách chương của một truyện, lọc theo trạng thái (null = tất cả), có phân trang
    @Operation(summary = "Lấy danh sách chương theo truyện", description = "Lấy danh sách chương của một truyện, lọc theo trạng thái (null = tất cả), có phân trang.")
    Page<ChapterResponse> getChaptersByComic(Long comicId, ChapterStatus status, Pageable pageable)
            throws Exception;

    // ---------- Quy trình duyệt ----------

    @Operation(summary = "Gửi chương đi duyệt", description = "Staff gửi duyệt: DRAFT -> PENDING_REVIEW, tạo ReviewRequest mới.")
    ChapterResponse submitForReview(Long chapterId, Long staffId) throws Exception;

    @Operation(summary = "Duyệt chương", description = "Admin duyệt: PENDING_REVIEW -> SCHEDULED (nếu có publishAt trong tương lai) hoặc PUBLISHED.")
    ChapterResponse approveChapter(Long chapterId, Long adminId) throws Exception;

    @Operation(summary = "Từ chối chương", description = "Admin từ chối: PENDING_REVIEW -> DRAFT, bắt buộc phải có lý do.")
    ChapterResponse rejectChapter(Long chapterId, Long adminId, String reason) throws Exception;

    // ---------- Hẹn giờ phát hành (BR10) ----------

    @Operation(summary = "Phát hành chương do hạn", description = "Scheduler gọi định kỳ: chuyển các chương SCHEDULED đã đến giờ sang PUBLISHED, trả về danh sách id vừa phát hành.")
    List<Long> publishDueChapters() throws Exception;
}
