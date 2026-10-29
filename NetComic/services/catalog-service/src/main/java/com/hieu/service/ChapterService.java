package com.hieu.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hieu.enums.ChapterStatus;
import com.hieu.payload.request.catalog.ChapterRequest;
import com.hieu.payload.response.catalog.ChapterResponse;

public interface ChapterService {


    ChapterResponse createChapter(Long comicId, ChapterRequest request) throws Exception;

    ChapterResponse updateChapter(Long id, ChapterRequest request) throws Exception;


    void deleteChapter(Long id) throws Exception;

    ChapterResponse getChapterById(Long id) throws Exception;

    // Danh sách chương của một truyện, lọc theo trạng thái (null = tất cả), có phân trang
    Page<ChapterResponse> getChaptersByComic(Long comicId, ChapterStatus status, Pageable pageable)
            throws Exception;

    // ---------- Quy trình duyệt ----------

    // Staff gửi duyệt: DRAFT  -> PENDING_REVIEW, tạo ReviewRequest
    ChapterResponse submitForReview(Long chapterId, Long staffId) throws Exception;

    // Admin duyệt: PENDING_REVIEW ->  SCHEDULED nếu có publishAt trong tương lai
    ChapterResponse approveChapter(Long chapterId, Long adminId) throws Exception;

    // Admin từ chối: PENDING_REVIEW -> REJECTED, bắt buộc có lý do
    ChapterResponse rejectChapter(Long chapterId, Long adminId, String reason) throws Exception;

    // ---------- Hẹn giờ phát hành (BR10) ----------

    // Scheduler gọi định kỳ: chuyển các chương SCHEDULED đã đến giờ sang PUBLISHED,
    // trả về danh sách id vừa phát hành để gửi event/thông báo
    List<Long> publishDueChapters() throws Exception;
}