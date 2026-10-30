package com.hieu.repository;

import com.hieu.enums.ChapterStatus;
import com.hieu.model.Chapter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    // 1. Kiểm tra trùng số chương trong cùng 1 truyện khi Tạo mới (khớp uk_chapter_comic_no)
    boolean existsByComicIdAndChapterNo(Long comicId, Integer chapterNo);

    // 2. Kiểm tra trùng số chương trong cùng 1 truyện khi Cập nhật (trừ chính chương đang sửa)
    boolean existsByComicIdAndChapterNoAndIdNot(Long comicId, Integer chapterNo, Long id);

    // 3. Lấy danh sách chương của 1 truyện khi status == null (lấy tất cả trạng thái), có phân trang
    Page<Chapter> findByComicId(Long comicId, Pageable pageable);

    // 4. Lấy danh sách chương của 1 truyện khi status != null (lọc theo trạng thái), có phân trang
    Page<Chapter> findByComicIdAndStatus(Long comicId, ChapterStatus status, Pageable pageable);

    // 5. Phục vụ BR10 (publishDueChapters): Lấy các chương đang SCHEDULED và đã đến/quá giờ publishAt
    List<Chapter> findByStatusAndPublishAtLessThanEqual(ChapterStatus status, LocalDateTime now);
}