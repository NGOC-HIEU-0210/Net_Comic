package com.hieu.service.impl;

import com.hieu.enums.ChapterStatus;
import com.hieu.enums.ReviewStatus;
import com.hieu.model.Chapter;
import com.hieu.model.Comic;
import com.hieu.model.ReviewRequest;
import com.hieu.payload.request.catalog.ReviewApproveRequest;
import com.hieu.payload.request.catalog.ReviewRejectRequest;
import com.hieu.payload.request.catalog.ReviewSubmitRequest;
import com.hieu.payload.response.catalog.ReviewRequestResponse;
import com.hieu.repository.ChapterRepository;
import com.hieu.repository.ComicAssignmentRepository;
import com.hieu.repository.ReviewRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewRequestServiceImplTest {

    private static final Pageable PAGEABLE = PageRequest.of(0, 10);

    @Mock
    private ReviewRequestRepository reviewRequestRepository;

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private ComicAssignmentRepository assignmentRepository;

    @InjectMocks
    private ReviewRequestServiceImpl service;

    // ---------- submit ----------

    @Test
    void submit_whenValid_createsPendingReviewAndMovesChapterToPendingReview() throws Exception {
        Chapter chapter = chapter(5L, 1L, ChapterStatus.DRAFT);
        when(chapterRepository.findById(5L)).thenReturn(Optional.of(chapter));
        when(assignmentRepository.existsById_ComicIdAndId_StaffId(1L, 7L)).thenReturn(true);
        when(reviewRequestRepository.existsByChapter_IdAndStatus(5L, ReviewStatus.PENDING)).thenReturn(false);
        when(reviewRequestRepository.save(any(ReviewRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReviewRequestResponse response = service.submit(submitRequest(5L, 7L));

        assertEquals(ReviewStatus.PENDING, response.getStatus());
        assertEquals(7L, response.getSubmittedBy());
        assertNull(response.getReviewedBy());
        assertEquals(5L, response.getChapterId());
        assertEquals(ChapterStatus.PENDING_REVIEW, response.getChapterStatus());

        ArgumentCaptor<ReviewRequest> captor = ArgumentCaptor.forClass(ReviewRequest.class);
        verify(reviewRequestRepository).save(captor.capture());
        assertEquals(chapter, captor.getValue().getChapter());
        assertEquals(7L, captor.getValue().getSubmittedBy());
        assertEquals(ReviewStatus.PENDING, captor.getValue().getStatus());

        assertEquals(ChapterStatus.PENDING_REVIEW, chapter.getStatus());
        verify(chapterRepository).save(chapter);
    }

    @Test
    void submit_whenChapterNotFound_throwsAndDoesNotSave() {
        when(chapterRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> service.submit(submitRequest(5L, 7L)));

        verify(reviewRequestRepository, never()).save(any(ReviewRequest.class));
        verify(chapterRepository, never()).save(any(Chapter.class));
    }

    @ParameterizedTest
    @EnumSource(value = ChapterStatus.class, names = "DRAFT", mode = EnumSource.Mode.EXCLUDE)
    void submit_whenChapterNotDraft_throwsAndKeepsStatus(ChapterStatus status) {
        Chapter chapter = chapter(5L, 1L, status);
        when(chapterRepository.findById(5L)).thenReturn(Optional.of(chapter));

        assertThrows(Exception.class, () -> service.submit(submitRequest(5L, 7L)));

        assertEquals(status, chapter.getStatus());
        verify(reviewRequestRepository, never()).save(any(ReviewRequest.class));
        verify(chapterRepository, never()).save(any(Chapter.class));
    }

    @Test
    void submit_whenChapterHasNoComic_throwsAndDoesNotSave() {
        Chapter chapter = chapter(5L, null, ChapterStatus.DRAFT);
        chapter.setComic(null);
        when(chapterRepository.findById(5L)).thenReturn(Optional.of(chapter));

        assertThrows(Exception.class, () -> service.submit(submitRequest(5L, 7L)));

        verify(reviewRequestRepository, never()).save(any(ReviewRequest.class));
    }

    @Test
    void submit_whenComicHasNoId_throwsAndDoesNotSave() {
        Chapter chapter = chapter(5L, null, ChapterStatus.DRAFT);
        chapter.setComic(new Comic());
        when(chapterRepository.findById(5L)).thenReturn(Optional.of(chapter));

        assertThrows(Exception.class, () -> service.submit(submitRequest(5L, 7L)));

        verify(reviewRequestRepository, never()).save(any(ReviewRequest.class));
    }

    @Test
    void submit_whenStaffNotAssignedToComic_throwsAndKeepsChapterDraft() {
        Chapter chapter = chapter(5L, 1L, ChapterStatus.DRAFT);
        when(chapterRepository.findById(5L)).thenReturn(Optional.of(chapter));
        when(assignmentRepository.existsById_ComicIdAndId_StaffId(1L, 7L)).thenReturn(false);

        assertThrows(Exception.class, () -> service.submit(submitRequest(5L, 7L)));

        assertEquals(ChapterStatus.DRAFT, chapter.getStatus());
        verify(reviewRequestRepository, never()).save(any(ReviewRequest.class));
        verify(chapterRepository, never()).save(any(Chapter.class));
    }

    @Test
    void submit_whenChapterAlreadyHasPendingRequest_throwsAndDoesNotSave() {
        Chapter chapter = chapter(5L, 1L, ChapterStatus.DRAFT);
        when(chapterRepository.findById(5L)).thenReturn(Optional.of(chapter));
        when(assignmentRepository.existsById_ComicIdAndId_StaffId(1L, 7L)).thenReturn(true);
        when(reviewRequestRepository.existsByChapter_IdAndStatus(5L, ReviewStatus.PENDING)).thenReturn(true);

        assertThrows(Exception.class, () -> service.submit(submitRequest(5L, 7L)));

        assertEquals(ChapterStatus.DRAFT, chapter.getStatus());
        verify(reviewRequestRepository, never()).save(any(ReviewRequest.class));
        verify(chapterRepository, never()).save(any(Chapter.class));
    }

    // ---------- getById ----------

    @Test
    void getById_whenFound_returnsMappedResponse() throws Exception {
        ReviewRequest review = review(10L, 5L, 7L, ReviewStatus.PENDING, ChapterStatus.PENDING_REVIEW);
        when(reviewRequestRepository.findById(10L)).thenReturn(Optional.of(review));

        ReviewRequestResponse response = service.getById(10L);

        assertEquals(10L, response.getId());
        assertEquals(5L, response.getChapterId());
        assertEquals(7L, response.getSubmittedBy());
        assertEquals(ReviewStatus.PENDING, response.getStatus());
    }

    @Test
    void getById_whenNotFound_throws() {
        when(reviewRequestRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> service.getById(10L));
    }

    // ---------- getAll / getPending / getByChapter / getByStaff ----------

    @Test
    void getAll_whenStatusNull_usesFindAll() {
        ReviewRequest review = review(10L, 5L, 7L, ReviewStatus.APPROVED, ChapterStatus.PUBLISHED);
        when(reviewRequestRepository.findAll(PAGEABLE)).thenReturn(new PageImpl<>(List.of(review), PAGEABLE, 1));

        Page<ReviewRequestResponse> result = service.getAll(null, PAGEABLE);

        assertEquals(1, result.getTotalElements());
        assertEquals(10L, result.getContent().get(0).getId());
        verify(reviewRequestRepository, never()).findByStatus(any(), any());
    }

    @Test
    void getAll_whenStatusGiven_filtersByStatus() {
        ReviewRequest review = review(10L, 5L, 7L, ReviewStatus.REJECTED, ChapterStatus.DRAFT);
        when(reviewRequestRepository.findByStatus(ReviewStatus.REJECTED, PAGEABLE))
                .thenReturn(new PageImpl<>(List.of(review), PAGEABLE, 1));

        Page<ReviewRequestResponse> result = service.getAll(ReviewStatus.REJECTED, PAGEABLE);

        assertEquals(ReviewStatus.REJECTED, result.getContent().get(0).getStatus());
        verify(reviewRequestRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getPending_alwaysFiltersByPending() {
        ReviewRequest review = review(10L, 5L, 7L, ReviewStatus.PENDING, ChapterStatus.PENDING_REVIEW);
        when(reviewRequestRepository.findByStatus(ReviewStatus.PENDING, PAGEABLE))
                .thenReturn(new PageImpl<>(List.of(review), PAGEABLE, 1));

        Page<ReviewRequestResponse> result = service.getPending(PAGEABLE);

        assertEquals(1, result.getTotalElements());
        assertEquals(ReviewStatus.PENDING, result.getContent().get(0).getStatus());
    }

    @Test
    void getByChapter_whenChapterExists_returnsPage() throws Exception {
        ReviewRequest review = review(10L, 5L, 7L, ReviewStatus.PENDING, ChapterStatus.PENDING_REVIEW);
        when(chapterRepository.existsById(5L)).thenReturn(true);
        when(reviewRequestRepository.findByChapter_Id(5L, PAGEABLE))
                .thenReturn(new PageImpl<>(List.of(review), PAGEABLE, 1));

        Page<ReviewRequestResponse> result = service.getByChapter(5L, PAGEABLE);

        assertEquals(1, result.getTotalElements());
        assertEquals(5L, result.getContent().get(0).getChapterId());
    }

    @Test
    void getByChapter_whenChapterNotFound_throwsAndDoesNotQuery() {
        when(chapterRepository.existsById(5L)).thenReturn(false);

        assertThrows(Exception.class, () -> service.getByChapter(5L, PAGEABLE));

        verify(reviewRequestRepository, never()).findByChapter_Id(any(), any());
    }

    @Test
    void getByStaff_returnsPageWithoutCheckingStaffExists() {
        ReviewRequest review = review(10L, 5L, 7L, ReviewStatus.PENDING, ChapterStatus.PENDING_REVIEW);
        when(reviewRequestRepository.findBySubmittedBy(7L, PAGEABLE))
                .thenReturn(new PageImpl<>(List.of(review), PAGEABLE, 1));

        Page<ReviewRequestResponse> result = service.getByStaff(7L, PAGEABLE);

        assertEquals(1, result.getTotalElements());
        assertEquals(7L, result.getContent().get(0).getSubmittedBy());
    }

    // ---------- approve ----------

    @Test
    void approve_whenPublishAtInFuture_schedulesChapter() throws Exception {
        ReviewRequest review = review(10L, 5L, 7L, ReviewStatus.PENDING, ChapterStatus.PENDING_REVIEW);
        review.getChapter().setPublishAt(LocalDateTime.now().plusDays(1));
        stubFindAndSave(review);

        ReviewRequestResponse response = service.approve(10L, approveRequest(99L));

        assertEquals(ChapterStatus.SCHEDULED, review.getChapter().getStatus());
        assertEquals(ChapterStatus.SCHEDULED, response.getChapterStatus());
        assertEquals(ReviewStatus.APPROVED, response.getStatus());
        assertEquals(99L, response.getReviewedBy());
        verify(chapterRepository).save(review.getChapter());
    }

    @Test
    void approve_whenPublishAtInPast_publishesImmediately() throws Exception {
        ReviewRequest review = review(10L, 5L, 7L, ReviewStatus.PENDING, ChapterStatus.PENDING_REVIEW);
        review.getChapter().setPublishAt(LocalDateTime.now().minusMinutes(1));
        stubFindAndSave(review);

        ReviewRequestResponse response = service.approve(10L, approveRequest(99L));

        assertEquals(ChapterStatus.PUBLISHED, review.getChapter().getStatus());
        assertEquals(ChapterStatus.PUBLISHED, response.getChapterStatus());
        assertEquals(ReviewStatus.APPROVED, response.getStatus());
    }

    @Test
    void approve_whenPublishAtNull_publishesImmediately() throws Exception {
        ReviewRequest review = review(10L, 5L, 7L, ReviewStatus.PENDING, ChapterStatus.PENDING_REVIEW);
        review.getChapter().setPublishAt(null);
        stubFindAndSave(review);

        service.approve(10L, approveRequest(99L));

        assertEquals(ChapterStatus.PUBLISHED, review.getChapter().getStatus());
    }

    @Test
    void approve_setsReviewerAndClearsReason() throws Exception {
        ReviewRequest review = review(10L, 5L, 7L, ReviewStatus.PENDING, ChapterStatus.PENDING_REVIEW);
        review.setReason("lý do cũ");
        stubFindAndSave(review);

        service.approve(10L, approveRequest(99L));

        ArgumentCaptor<ReviewRequest> captor = ArgumentCaptor.forClass(ReviewRequest.class);
        verify(reviewRequestRepository).save(captor.capture());
        assertEquals(ReviewStatus.APPROVED, captor.getValue().getStatus());
        assertEquals(99L, captor.getValue().getReviewedBy());
        assertNull(captor.getValue().getReason());
    }

    @Test
    void approve_whenReviewNotFound_throws() {
        when(reviewRequestRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> service.approve(10L, approveRequest(99L)));

        verify(reviewRequestRepository, never()).save(any(ReviewRequest.class));
        verify(chapterRepository, never()).save(any(Chapter.class));
    }

    @ParameterizedTest
    @EnumSource(value = ReviewStatus.class, names = "PENDING", mode = EnumSource.Mode.EXCLUDE)
    void approve_whenReviewAlreadyProcessed_throwsAndDoesNotSave(ReviewStatus status) {
        ReviewRequest review = review(10L, 5L, 7L, status, ChapterStatus.PENDING_REVIEW);
        when(reviewRequestRepository.findById(10L)).thenReturn(Optional.of(review));

        assertThrows(Exception.class, () -> service.approve(10L, approveRequest(99L)));

        assertEquals(status, review.getStatus());
        verify(reviewRequestRepository, never()).save(any(ReviewRequest.class));
        verify(chapterRepository, never()).save(any(Chapter.class));
    }

    @Test
    void approve_whenReviewHasNoChapter_throws() {
        ReviewRequest review = review(10L, 5L, 7L, ReviewStatus.PENDING, ChapterStatus.PENDING_REVIEW);
        review.setChapter(null);
        when(reviewRequestRepository.findById(10L)).thenReturn(Optional.of(review));

        assertThrows(Exception.class, () -> service.approve(10L, approveRequest(99L)));

        verify(reviewRequestRepository, never()).save(any(ReviewRequest.class));
    }

    @ParameterizedTest
    @EnumSource(value = ChapterStatus.class, names = "PENDING_REVIEW", mode = EnumSource.Mode.EXCLUDE)
    void approve_whenChapterNotPendingReview_throwsAndKeepsPending(ChapterStatus chapterStatus) {
        ReviewRequest review = review(10L, 5L, 7L, ReviewStatus.PENDING, chapterStatus);
        when(reviewRequestRepository.findById(10L)).thenReturn(Optional.of(review));

        assertThrows(Exception.class, () -> service.approve(10L, approveRequest(99L)));

        assertEquals(ReviewStatus.PENDING, review.getStatus());
        assertEquals(chapterStatus, review.getChapter().getStatus());
        verify(reviewRequestRepository, never()).save(any(ReviewRequest.class));
    }

    // ---------- reject ----------

    @Test
    void reject_whenValid_returnsChapterToDraftAndStoresReason() throws Exception {
        ReviewRequest review = review(10L, 5L, 7L, ReviewStatus.PENDING, ChapterStatus.PENDING_REVIEW);
        stubFindAndSave(review);

        ReviewRequestResponse response = service.reject(10L, rejectRequest(99L, "Ảnh bị mờ"));

        assertEquals(ReviewStatus.REJECTED, response.getStatus());
        assertEquals(99L, response.getReviewedBy());
        assertEquals("Ảnh bị mờ", response.getReason());
        assertEquals(ChapterStatus.DRAFT, review.getChapter().getStatus());
        assertEquals(ChapterStatus.DRAFT, response.getChapterStatus());
        verify(chapterRepository).save(review.getChapter());
    }

    @Test
    void reject_trimsReason() throws Exception {
        ReviewRequest review = review(10L, 5L, 7L, ReviewStatus.PENDING, ChapterStatus.PENDING_REVIEW);
        stubFindAndSave(review);

        ReviewRequestResponse response = service.reject(10L, rejectRequest(99L, "   Sai thứ tự trang  "));

        assertEquals("Sai thứ tự trang", response.getReason());
    }

    @Test
    void reject_whenReasonIsExactlyMaxLength_isAccepted() throws Exception {
        ReviewRequest review = review(10L, 5L, 7L, ReviewStatus.PENDING, ChapterStatus.PENDING_REVIEW);
        stubFindAndSave(review);

        ReviewRequestResponse response = service.reject(10L, rejectRequest(99L, "a".repeat(1000)));

        assertEquals(1000, response.getReason().length());
        assertEquals(ReviewStatus.REJECTED, response.getStatus());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t\n"})
    void reject_whenReasonBlank_throwsAndDoesNotChangeState(String reason) {
        ReviewRequest review = review(10L, 5L, 7L, ReviewStatus.PENDING, ChapterStatus.PENDING_REVIEW);
        when(reviewRequestRepository.findById(10L)).thenReturn(Optional.of(review));

        assertThrows(Exception.class, () -> service.reject(10L, rejectRequest(99L, reason)));

        assertEquals(ReviewStatus.PENDING, review.getStatus());
        assertEquals(ChapterStatus.PENDING_REVIEW, review.getChapter().getStatus());
        verify(reviewRequestRepository, never()).save(any(ReviewRequest.class));
        verify(chapterRepository, never()).save(any(Chapter.class));
    }

    @Test
    void reject_whenReasonTooLong_throwsAndDoesNotChangeState() {
        ReviewRequest review = review(10L, 5L, 7L, ReviewStatus.PENDING, ChapterStatus.PENDING_REVIEW);
        when(reviewRequestRepository.findById(10L)).thenReturn(Optional.of(review));

        assertThrows(Exception.class, () -> service.reject(10L, rejectRequest(99L, "a".repeat(1001))));

        assertEquals(ReviewStatus.PENDING, review.getStatus());
        verify(reviewRequestRepository, never()).save(any(ReviewRequest.class));
        verify(chapterRepository, never()).save(any(Chapter.class));
    }

    @Test
    void reject_whenReviewNotFound_throws() {
        when(reviewRequestRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> service.reject(10L, rejectRequest(99L, "lý do")));

        verify(reviewRequestRepository, never()).save(any(ReviewRequest.class));
    }

    @ParameterizedTest
    @EnumSource(value = ReviewStatus.class, names = "PENDING", mode = EnumSource.Mode.EXCLUDE)
    void reject_whenReviewAlreadyProcessed_throwsAndDoesNotSave(ReviewStatus status) {
        ReviewRequest review = review(10L, 5L, 7L, status, ChapterStatus.PENDING_REVIEW);
        when(reviewRequestRepository.findById(10L)).thenReturn(Optional.of(review));

        assertThrows(Exception.class, () -> service.reject(10L, rejectRequest(99L, "lý do")));

        assertEquals(status, review.getStatus());
        verify(reviewRequestRepository, never()).save(any(ReviewRequest.class));
    }

    @Test
    void reject_whenChapterNotPendingReview_throwsAndDoesNotSave() {
        ReviewRequest review = review(10L, 5L, 7L, ReviewStatus.PENDING, ChapterStatus.PUBLISHED);
        when(reviewRequestRepository.findById(10L)).thenReturn(Optional.of(review));

        assertThrows(Exception.class, () -> service.reject(10L, rejectRequest(99L, "lý do")));

        assertEquals(ChapterStatus.PUBLISHED, review.getChapter().getStatus());
        verify(reviewRequestRepository, never()).save(any(ReviewRequest.class));
        verify(chapterRepository, never()).save(any(Chapter.class));
    }

    // ---------- cancel ----------

    @Test
    void cancel_whenOwnerAndPending_deletesReviewAndReturnsChapterToDraft() throws Exception {
        ReviewRequest review = review(10L, 5L, 7L, ReviewStatus.PENDING, ChapterStatus.PENDING_REVIEW);
        when(reviewRequestRepository.findById(10L)).thenReturn(Optional.of(review));

        service.cancel(10L, 7L);

        assertEquals(ChapterStatus.DRAFT, review.getChapter().getStatus());
        verify(chapterRepository).save(review.getChapter());
        verify(reviewRequestRepository).delete(review);
    }

    @Test
    void cancel_whenChapterNoLongerPendingReview_deletesReviewWithoutTouchingChapter() throws Exception {
        ReviewRequest review = review(10L, 5L, 7L, ReviewStatus.PENDING, ChapterStatus.DRAFT);
        when(reviewRequestRepository.findById(10L)).thenReturn(Optional.of(review));

        service.cancel(10L, 7L);

        assertEquals(ChapterStatus.DRAFT, review.getChapter().getStatus());
        verify(chapterRepository, never()).save(any(Chapter.class));
        verify(reviewRequestRepository).delete(review);
    }

    @Test
    void cancel_whenStaffIdNull_throwsBeforeLookup() {
        assertThrows(Exception.class, () -> service.cancel(10L, null));

        verify(reviewRequestRepository, never()).findById(any());
        verify(reviewRequestRepository, never()).delete(any(ReviewRequest.class));
    }

    @Test
    void cancel_whenReviewNotFound_throws() {
        when(reviewRequestRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> service.cancel(10L, 7L));

        verify(reviewRequestRepository, never()).delete(any(ReviewRequest.class));
    }

    @ParameterizedTest
    @EnumSource(value = ReviewStatus.class, names = "PENDING", mode = EnumSource.Mode.EXCLUDE)
    void cancel_whenReviewAlreadyProcessed_throwsAndDoesNotDelete(ReviewStatus status) {
        ReviewRequest review = review(10L, 5L, 7L, status, ChapterStatus.PUBLISHED);
        when(reviewRequestRepository.findById(10L)).thenReturn(Optional.of(review));

        assertThrows(Exception.class, () -> service.cancel(10L, 7L));

        verify(reviewRequestRepository, never()).delete(any(ReviewRequest.class));
        verify(chapterRepository, never()).save(any(Chapter.class));
    }

    @Test
    void cancel_whenRequesterIsNotSubmitter_throwsAndDoesNotDelete() {
        ReviewRequest review = review(10L, 5L, 7L, ReviewStatus.PENDING, ChapterStatus.PENDING_REVIEW);
        when(reviewRequestRepository.findById(10L)).thenReturn(Optional.of(review));

        assertThrows(Exception.class, () -> service.cancel(10L, 8L));

        assertEquals(ChapterStatus.PENDING_REVIEW, review.getChapter().getStatus());
        verify(reviewRequestRepository, never()).delete(any(ReviewRequest.class));
        verify(chapterRepository, never()).save(any(Chapter.class));
    }

    // ---------- helpers ----------

    private void stubFindAndSave(ReviewRequest review) {
        when(reviewRequestRepository.findById(review.getId())).thenReturn(Optional.of(review));
        when(reviewRequestRepository.save(any(ReviewRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static Comic comic(Long id) {
        Comic comic = new Comic();
        comic.setId(id);
        comic.setTitle("One Piece");
        return comic;
    }

    private static Chapter chapter(Long id, Long comicId, ChapterStatus status) {
        Chapter chapter = new Chapter();
        chapter.setId(id);
        chapter.setChapterNo(1);
        chapter.setTitle("Chapter 1");
        chapter.setStatus(status);
        if (comicId != null) {
            chapter.setComic(comic(comicId));
        }
        return chapter;
    }

    private static ReviewRequest review(Long id, Long chapterId, Long submittedBy,
                                        ReviewStatus status, ChapterStatus chapterStatus) {
        ReviewRequest review = new ReviewRequest();
        review.setId(id);
        review.setChapter(chapter(chapterId, 1L, chapterStatus));
        review.setSubmittedBy(submittedBy);
        review.setStatus(status);
        return review;
    }

    private static ReviewSubmitRequest submitRequest(Long chapterId, Long staffId) {
        ReviewSubmitRequest request = new ReviewSubmitRequest();
        request.setChapterId(chapterId);
        request.setStaffId(staffId);
        return request;
    }

    private static ReviewApproveRequest approveRequest(Long adminId) {
        ReviewApproveRequest request = new ReviewApproveRequest();
        request.setAdminId(adminId);
        return request;
    }

    private static ReviewRejectRequest rejectRequest(Long adminId, String reason) {
        ReviewRejectRequest request = new ReviewRejectRequest();
        request.setAdminId(adminId);
        request.setReason(reason);
        return request;
    }
}