package com.hieu.service.impl;

import com.hieu.enums.ChapterStatus;
import com.hieu.enums.ReviewStatus;
import com.hieu.mapper.ReviewRequestMapper;
import com.hieu.model.Chapter;
import com.hieu.model.ReviewRequest;
import com.hieu.payload.request.catalog.ReviewApproveRequest;
import com.hieu.payload.request.catalog.ReviewRejectRequest;
import com.hieu.payload.request.catalog.ReviewSubmitRequest;
import com.hieu.payload.response.catalog.ReviewRequestResponse;
import com.hieu.repository.ChapterRepository;
import com.hieu.repository.ComicAssignmentRepository;
import com.hieu.repository.ReviewRequestRepository;
import com.hieu.service.ReviewRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ReviewRequestServiceImpl implements ReviewRequestService {

    private final ReviewRequestRepository reviewRequestRepository;
    private final ChapterRepository chapterRepository;
    private final ComicAssignmentRepository assignmentRepository;

    public ReviewRequestServiceImpl(ReviewRequestRepository reviewRequestRepository,
                                    ChapterRepository chapterRepository,
                                    ComicAssignmentRepository assignmentRepository) {
        this.reviewRequestRepository = reviewRequestRepository;
        this.chapterRepository = chapterRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewRequestResponse submit(ReviewSubmitRequest request) throws Exception {
        Chapter chapter = chapterRepository.findById(request.getChapterId()).orElseThrow(
                () -> new Exception("Không tìm thấy chương với id = " + request.getChapterId())
        );

        if (chapter.getStatus() != ChapterStatus.DRAFT) {
            throw new Exception("Chỉ gửi duyệt chương đang ở trạng thái nháp");
        }
        if (chapter.getComic() == null || chapter.getComic().getId() == null) {
            throw new Exception("Chương không thuộc truyện nào");
        }

        Long comicId = chapter.getComic().getId();
        // BR07: staff chỉ thao tác truyện mình được phân công
        if (!assignmentRepository.existsById_ComicIdAndId_StaffId(comicId, request.getStaffId())) {
            throw new Exception("Nhân viên chưa được phân công truyện này");
        }
        if (reviewRequestRepository.existsByChapter_IdAndStatus(chapter.getId(), ReviewStatus.PENDING)) {
            throw new Exception("Chương đang có yêu cầu chờ duyệt");
        }

        ReviewRequest review = ReviewRequestMapper.toEntity(request, chapter);

        chapter.setStatus(ChapterStatus.PENDING_REVIEW);

        ReviewRequest saved = reviewRequestRepository.save(review);
        chapterRepository.save(chapter);
        return ReviewRequestMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewRequestResponse getById(Long id) throws Exception {
        return ReviewRequestMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewRequestResponse> getAll(ReviewStatus status, Pageable pageable) {
        Page<ReviewRequest> page = status == null
                ? reviewRequestRepository.findAll(pageable)
                : reviewRequestRepository.findByStatus(status, pageable);
        return page.map(ReviewRequestMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewRequestResponse> getByChapter(Long chapterId, Pageable pageable) throws Exception {
        if (!chapterRepository.existsById(chapterId)) {
            throw new Exception("Không tìm thấy chương với id = " + chapterId);
        }
        return reviewRequestRepository.findByChapter_Id(chapterId, pageable)
                .map(ReviewRequestMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewRequestResponse> getByStaff(Long staffId, Pageable pageable) {
        // Không kiểm tra staffId tồn tại: tài khoản nằm ở Identity Service
        return reviewRequestRepository.findBySubmittedBy(staffId, pageable)
                .map(ReviewRequestMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewRequestResponse> getPending(Pageable pageable) {
        return reviewRequestRepository.findByStatus(ReviewStatus.PENDING, pageable)
                .map(ReviewRequestMapper::toResponse);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewRequestResponse approve(Long id, ReviewApproveRequest request) throws Exception {
        ReviewRequest review = findPendingOrThrow(id);
        Chapter chapter = review.getChapter();

        LocalDateTime publishAt = chapter.getPublishAt();
        if (publishAt != null && publishAt.isAfter(LocalDateTime.now())) {
            // BR10: chưa tới giờ thì hẹn, scheduler sẽ chuyển PUBLISHED sau
            chapter.setStatus(ChapterStatus.SCHEDULED);
        } else {
            chapter.setStatus(ChapterStatus.PUBLISHED);
        }

        review.setStatus(ReviewStatus.APPROVED);
        review.setReviewedBy(request.getAdminId());
        review.setReason(null);

        chapterRepository.save(chapter);
        return ReviewRequestMapper.toResponse(reviewRequestRepository.save(review));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewRequestResponse reject(Long id, ReviewRejectRequest request) throws Exception {
        ReviewRequest review = findPendingOrThrow(id);
        Chapter chapter = review.getChapter();

        String reason = request.getReason() == null ? "" : request.getReason().trim();
        if (reason.isEmpty()) {
            throw new Exception("Lý do từ chối không được để trống");
        }
        if (reason.length() > 1000) {
            throw new Exception("Lý do không được dài quá 1000 ký tự");
        }

        // Enum chương không có REJECTED. Trả về DRAFT để staff sửa rồi gửi phiếu mới
        chapter.setStatus(ChapterStatus.DRAFT);
        review.setStatus(ReviewStatus.REJECTED);
        review.setReviewedBy(request.getAdminId());
        review.setReason(reason);

        chapterRepository.save(chapter);
        return ReviewRequestMapper.toResponse(reviewRequestRepository.save(review));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id, Long staffId) throws Exception {
        if (staffId == null) {
            throw new Exception("staffId không được để trống");
        }

        ReviewRequest review = findOrThrow(id);
        if (review.getStatus() != ReviewStatus.PENDING) {
            throw new Exception("Chỉ hủy được yêu cầu đang chờ duyệt");
        }
        if (!review.getSubmittedBy().equals(staffId)) {
            throw new Exception("Chỉ nhân viên đã gửi yêu cầu mới được hủy");
        }

        Chapter chapter = review.getChapter();
        if (chapter.getStatus() == ChapterStatus.PENDING_REVIEW) {
            chapter.setStatus(ChapterStatus.DRAFT);
            chapterRepository.save(chapter);
        }
        reviewRequestRepository.delete(review);
    }

    private ReviewRequest findOrThrow(Long id) throws Exception {
        return reviewRequestRepository.findById(id).orElseThrow(
                () -> new Exception("Không tìm thấy yêu cầu duyệt với id = " + id)
        );
    }

    private ReviewRequest findPendingOrThrow(Long id) throws Exception {
        ReviewRequest review = findOrThrow(id);
        if (review.getStatus() != ReviewStatus.PENDING) {
            throw new Exception("Yêu cầu duyệt đã được xử lý");
        }
        if (review.getChapter() == null) {
            throw new Exception("Yêu cầu duyệt không gắn với chương");
        }
        if (review.getChapter().getStatus() != ChapterStatus.PENDING_REVIEW) {
            throw new Exception("Chương không đang ở trạng thái chờ duyệt");
        }
        return review;
    }
}