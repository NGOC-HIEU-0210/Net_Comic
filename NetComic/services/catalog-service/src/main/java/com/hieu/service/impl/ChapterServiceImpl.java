package com.hieu.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hieu.enums.ChapterStatus;
import com.hieu.enums.ReviewStatus;
import com.hieu.mapper.ChapterMapper;
import com.hieu.model.Chapter;
import com.hieu.model.Comic;
import com.hieu.model.ReviewRequest;
import com.hieu.payload.request.catalog.ChapterRequest;
import com.hieu.payload.response.catalog.ChapterResponse;
import com.hieu.repository.ChapterRepository;
import com.hieu.repository.ComicAssignmentRepository;
import com.hieu.repository.ComicRepository;
import com.hieu.repository.ReviewRequestRepository;
import com.hieu.service.ChapterService;

@Service
public class ChapterServiceImpl implements ChapterService {

    private final ChapterRepository chapterRepository;
    private final ComicRepository comicRepository;
    private final ComicAssignmentRepository assignmentRepository;
    private final ReviewRequestRepository reviewRequestRepository;

    public ChapterServiceImpl(ChapterRepository chapterRepository,
                              ComicRepository comicRepository,
                              ComicAssignmentRepository assignmentRepository,
                              ReviewRequestRepository reviewRequestRepository) {
        this.chapterRepository = chapterRepository;
        this.comicRepository = comicRepository;
        this.assignmentRepository = assignmentRepository;
        this.reviewRequestRepository = reviewRequestRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChapterResponse createChapter(Long comicId, ChapterRequest request) throws Exception {
        Comic comic = comicRepository.findById(comicId).orElseThrow(
                () -> new Exception("Không tìm thấy truyện với id = " + comicId)
        );

        if (chapterRepository.existsByComicIdAndChapterNo(comicId, request.getChapterNo())) {
            throw new Exception("Truyện với id = " + comicId + " đã có chương số " + request.getChapterNo());
        }

        Chapter saved = chapterRepository.save(ChapterMapper.toEntity(request, comic));
        return ChapterMapper.toResponse(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChapterResponse updateChapter(Long id, ChapterRequest request) throws Exception {
        Chapter chapter = findOrThrow(id);

        Comic comic = comicRepository.findById(request.getComicId()).orElseThrow(
                () -> new Exception("Không tìm thấy truyện với id = " + request.getComicId())
        );

        if (chapterRepository.existsByComicIdAndChapterNoAndIdNot(request.getComicId(), request.getChapterNo(), id)) {
            throw new Exception("Truyện với id = " + request.getComicId() + " đã có chương số " + request.getChapterNo());
        }

        ChapterMapper.updateEntity(chapter, request, comic);
        return ChapterMapper.toResponse(chapterRepository.save(chapter));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChapter(Long id) throws Exception {
        chapterRepository.delete(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ChapterResponse getChapterById(Long id) throws Exception {
        return ChapterMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChapterResponse> getChaptersByComic(Long comicId, ChapterStatus status, Pageable pageable) throws Exception {
        if (!comicRepository.existsById(comicId)) {
            throw new Exception("Không tìm thấy truyện với id = " + comicId);
        }
        Page<Chapter> page = status == null
                ? chapterRepository.findByComicId(comicId, pageable)
                : chapterRepository.findByComicIdAndStatus(comicId, status, pageable);
        return page.map(ChapterMapper::toResponse);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChapterResponse submitForReview(Long chapterId, Long staffId) throws Exception {
        Chapter chapter = findOrThrow(chapterId);

        if (chapter.getStatus() != ChapterStatus.DRAFT) {
            throw new Exception("Chỉ gửi duyệt chương đang ở trạng thái nháp");
        }
        if (chapter.getComic() == null || chapter.getComic().getId() == null) {
            throw new Exception("Chương không thuộc truyện nào");
        }

        Long comicId = chapter.getComic().getId();
        // BR07: staff chỉ thao tác truyện mình được phân công
        if (!assignmentRepository.existsById_ComicIdAndId_StaffId(comicId, staffId)) {
            throw new Exception("Nhân viên chưa được phân công truyện này");
        }
        if (reviewRequestRepository.existsByChapter_IdAndStatus(chapterId, ReviewStatus.PENDING)) {
            throw new Exception("Chương đang có yêu cầu chờ duyệt");
        }

        ReviewRequest review = new ReviewRequest();
        review.setChapter(chapter);
        review.setSubmittedBy(staffId);
        review.setStatus(ReviewStatus.PENDING);

        chapter.setStatus(ChapterStatus.PENDING_REVIEW);
        chapterRepository.save(chapter);
        reviewRequestRepository.save(review);

        return ChapterMapper.toResponse(chapter);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChapterResponse approveChapter(Long chapterId, Long adminId) throws Exception {
        Chapter chapter = findOrThrow(chapterId);
        ReviewRequest review = findPendingReviewOrThrow(chapterId);

        LocalDateTime publishAt = chapter.getPublishAt();
        if (publishAt != null && publishAt.isAfter(LocalDateTime.now())) {
            // BR10: chưa tới giờ thì hẹn, scheduler sẽ chuyển sang PUBLISHED sau
            chapter.setStatus(ChapterStatus.SCHEDULED);
        } else {
            chapter.setStatus(ChapterStatus.PUBLISHED);
        }

        chapterRepository.save(chapter);

        review.setStatus(ReviewStatus.APPROVED);
        review.setReviewedBy(adminId);
        review.setReason(null);
        reviewRequestRepository.save(review);

        return ChapterMapper.toResponse(chapter);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChapterResponse rejectChapter(Long chapterId, Long adminId, String reason) throws Exception {
        Chapter chapter = findOrThrow(chapterId);
        ReviewRequest review = findPendingReviewOrThrow(chapterId);

        String trimmedReason = reason == null ? "" : reason.trim();
        if (trimmedReason.isEmpty()) {
            throw new Exception("Lý do từ chối không được để trống");
        }
        if (trimmedReason.length() > 1000) {
            throw new Exception("Lý do không được dài quá 1000 ký tự");
        }

        // Enum ChapterStatus không có REJECTED: trả về DRAFT để staff sửa rồi gửi lại
        chapter.setStatus(ChapterStatus.DRAFT);
        review.setStatus(ReviewStatus.REJECTED);
        review.setReviewedBy(adminId);
        review.setReason(trimmedReason);
        reviewRequestRepository.save(review);
        chapterRepository.save(chapter);

        return ChapterMapper.toResponse(chapter);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> publishDueChapters() throws Exception {
        List<Long> publishedIds = new ArrayList<>();
        List<Chapter> dueChapters = chapterRepository.findByStatusAndPublishAtLessThanEqual(ChapterStatus.SCHEDULED, LocalDateTime.now());
        for (Chapter chapter : dueChapters) {
            chapter.setStatus(ChapterStatus.PUBLISHED);
            chapterRepository.save(chapter);
            publishedIds.add(chapter.getId());
        }
        return publishedIds;
    }

    private Chapter findOrThrow(Long id) throws Exception {
        return chapterRepository.findById(id).orElseThrow(
                () -> new Exception("Không tìm thấy chương với id = " + id)
        );
    }

    private ReviewRequest findPendingReviewOrThrow(Long chapterId) throws Exception {
        ReviewRequest review = reviewRequestRepository.findByChapter_IdAndStatus(chapterId, ReviewStatus.PENDING)
                .orElseThrow(() -> new Exception("Chương với id = " + chapterId + " không có yêu cầu duyệt đang chờ xử lý"));
        if (review.getChapter() == null) {
            throw new Exception("Yêu cầu duyệt không gắn với chương");
        }
        if (review.getChapter().getStatus() != ChapterStatus.PENDING_REVIEW) {
            throw new Exception("Chương không đang ở trạng thái chờ duyệt");
        }
        return review;
    }
}
