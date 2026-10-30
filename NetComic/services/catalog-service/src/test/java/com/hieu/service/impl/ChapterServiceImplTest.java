package com.hieu.service.impl;

import com.hieu.enums.ChapterStatus;
import com.hieu.enums.ReviewStatus;
import com.hieu.model.Chapter;
import com.hieu.model.Comic;
import com.hieu.model.ReviewRequest;
import com.hieu.payload.request.catalog.ChapterRequest;
import com.hieu.payload.response.catalog.ChapterResponse;
import com.hieu.repository.ChapterRepository;
import com.hieu.repository.ComicAssignmentRepository;
import com.hieu.repository.ComicRepository;
import com.hieu.repository.ReviewRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChapterServiceImplTest {

    private static final Pageable PAGEABLE = PageRequest.of(0, 10);

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private ComicRepository comicRepository;

    @Mock
    private ComicAssignmentRepository assignmentRepository;

    @Mock
    private ReviewRequestRepository reviewRequestRepository;

    @InjectMocks
    private ChapterServiceImpl service;

    // ---------- createChapter ----------

    @Test
    void createChapter_whenComicExists_savesChapterAndReturnsResponse() throws Exception {
        when(comicRepository.findById(1L)).thenReturn(Optional.of(comic(1L)));
        when(chapterRepository.existsByComicIdAndChapterNo(1L, 1)).thenReturn(false);
        when(chapterRepository.save(any(Chapter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChapterRequest request = new ChapterRequest();
        request.setComicId(1L);
        request.setChapterNo(1);
        request.setTitle("Chapter 1");
        request.setPriceCoins(0);
        request.setStatus(ChapterStatus.DRAFT);

        ChapterResponse response = service.createChapter(1L, request);

        assertEquals(1, response.getChapterNo());
        assertEquals(ChapterStatus.DRAFT, response.getStatus());
        assertEquals(0, response.getPriceCoins());
        verify(chapterRepository).save(any(Chapter.class));
    }

    @Test
    void createChapter_whenComicNotFound_throws() {
        when(comicRepository.findById(1L)).thenReturn(Optional.empty());

        ChapterRequest request = new ChapterRequest();
        request.setComicId(1L);
        request.setChapterNo(1);
        request.setPriceCoins(0);
        request.setStatus(ChapterStatus.DRAFT);

        assertThrows(Exception.class, () -> service.createChapter(1L, request));

        verify(chapterRepository, never()).save(any(Chapter.class));
    }

    @Test
    void createChapter_whenChapterNoAlreadyExists_throws() {
        when(comicRepository.findById(1L)).thenReturn(Optional.of(comic(1L)));
        when(chapterRepository.existsByComicIdAndChapterNo(1L, 1)).thenReturn(true);

        ChapterRequest request = new ChapterRequest();
        request.setComicId(1L);
        request.setChapterNo(1);
        request.setPriceCoins(0);
        request.setStatus(ChapterStatus.DRAFT);

        assertThrows(Exception.class, () -> service.createChapter(1L, request));

        verify(chapterRepository, never()).save(any(Chapter.class));
    }

    // ---------- updateChapter ----------

    @Test
    void updateChapter_whenValid_updatesAndReturnsResponse() throws Exception {
        Chapter chapter = chapter(5L, 1L, ChapterStatus.DRAFT);
        when(chapterRepository.findById(5L)).thenReturn(Optional.of(chapter));
        when(comicRepository.findById(1L)).thenReturn(Optional.of(comic(1L)));
        when(chapterRepository.existsByComicIdAndChapterNoAndIdNot(1L, 2, 5L)).thenReturn(false);
        when(chapterRepository.save(any(Chapter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChapterRequest request = new ChapterRequest();
        request.setComicId(1L);
        request.setChapterNo(2);
        request.setTitle("Chapter 2");
        request.setPriceCoins(0);
        request.setStatus(ChapterStatus.DRAFT);

        ChapterResponse response = service.updateChapter(5L, request);

        assertEquals(2, response.getChapterNo());
        assertEquals("Chapter 2", response.getTitle());
        verify(chapterRepository).save(chapter);
    }

    @Test
    void updateChapter_whenDuplicateChapterNo_throwsAndDoesNotSave() {
        Chapter chapter = chapter(5L, 1L, ChapterStatus.DRAFT);
        when(chapterRepository.findById(5L)).thenReturn(Optional.of(chapter));
        when(comicRepository.findById(1L)).thenReturn(Optional.of(comic(1L)));
        when(chapterRepository.existsByComicIdAndChapterNoAndIdNot(1L, 2, 5L)).thenReturn(true);

        ChapterRequest request = new ChapterRequest();
        request.setComicId(1L);
        request.setChapterNo(2);
        request.setPriceCoins(0);
        request.setStatus(ChapterStatus.DRAFT);

        assertThrows(Exception.class, () -> service.updateChapter(5L, request));

        verify(chapterRepository, never()).save(any(Chapter.class));
    }

    // ---------- deleteChapter ----------

    @Test
    void deleteChapter_whenChapterExists_deletesChapter() throws Exception {
        when(chapterRepository.findById(5L)).thenReturn(Optional.of(chapter(5L, 1L, ChapterStatus.DRAFT)));

        service.deleteChapter(5L);

        verify(chapterRepository).delete(any(Chapter.class));
    }

    // ---------- getChapterById ----------

    @Test
    void getChapterById_whenFound_returnsMappedResponse() throws Exception {
        when(chapterRepository.findById(5L)).thenReturn(Optional.of(chapter(5L, 1L, ChapterStatus.DRAFT)));

        ChapterResponse response = service.getChapterById(5L);

        assertEquals(5L, response.getId());
        assertEquals(1L, response.getComicId());
        assertEquals("One Piece", response.getComicTitle());
    }

    @Test
    void getChapterById_whenNotFound_throws() {
        when(chapterRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> service.getChapterById(5L));
    }

    // ---------- getChaptersByComic ----------

    @Test
    void getChaptersByComic_whenComicNotFound_throws() {
        when(comicRepository.existsById(1L)).thenReturn(false);

        assertThrows(Exception.class, () -> service.getChaptersByComic(1L, null, PAGEABLE));
    }

    @Test
    void getChaptersByComic_whenStatusNull_returnsAllChapters() throws Exception {
        when(comicRepository.existsById(1L)).thenReturn(true);
        when(chapterRepository.findByComicId(1L, PAGEABLE))
                .thenReturn(new PageImpl<>(List.of(chapter(5L, 1L, ChapterStatus.DRAFT)), PAGEABLE, 1));

        Page<ChapterResponse> result = service.getChaptersByComic(1L, null, PAGEABLE);

        assertEquals(1, result.getTotalElements());
        assertEquals(5L, result.getContent().get(0).getId());
    }

    @Test
    void getChaptersByComic_whenStatusGiven_filtersByStatus() throws Exception {
        when(comicRepository.existsById(1L)).thenReturn(true);
        when(chapterRepository.findByComicIdAndStatus(1L, ChapterStatus.PUBLISHED, PAGEABLE))
                .thenReturn(new PageImpl<>(List.of(chapter(5L, 1L, ChapterStatus.PUBLISHED)), PAGEABLE, 1));

        Page<ChapterResponse> result = service.getChaptersByComic(1L, ChapterStatus.PUBLISHED, PAGEABLE);

        assertEquals(ChapterStatus.PUBLISHED, result.getContent().get(0).getStatus());
    }

    // ---------- submitForReview ----------

    @Test
    void submitForReview_whenValid_createsPendingReviewAndMovesChapterToPendingReview() throws Exception {
        Chapter chapter = chapter(5L, 1L, ChapterStatus.DRAFT);
        when(chapterRepository.findById(5L)).thenReturn(Optional.of(chapter));
        when(assignmentRepository.existsById_ComicIdAndId_StaffId(1L, 7L)).thenReturn(true);
        when(reviewRequestRepository.existsByChapter_IdAndStatus(5L, ReviewStatus.PENDING)).thenReturn(false);
        when(reviewRequestRepository.save(any(ReviewRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChapterResponse response = service.submitForReview(5L, 7L);

        assertEquals(ChapterStatus.PENDING_REVIEW, chapter.getStatus());
        assertEquals(ChapterStatus.PENDING_REVIEW, response.getStatus());
        verify(reviewRequestRepository).save(any(ReviewRequest.class));
        verify(chapterRepository).save(chapter);
    }

    @Test
    void submitForReview_whenChapterNotDraft_throwsAndKeepsStatus() {
        Chapter chapter = chapter(5L, 1L, ChapterStatus.PUBLISHED);
        when(chapterRepository.findById(5L)).thenReturn(Optional.of(chapter));

        assertThrows(Exception.class, () -> service.submitForReview(5L, 7L));

        assertEquals(ChapterStatus.PUBLISHED, chapter.getStatus());
        verify(reviewRequestRepository, never()).save(any(ReviewRequest.class));
        verify(chapterRepository, never()).save(any(Chapter.class));
    }

    @Test
    void submitForReview_whenChapterHasNoComic_throwsAndDoesNotSave() {
        Chapter chapter = chapter(5L, null, ChapterStatus.DRAFT);
        chapter.setComic(null);
        when(chapterRepository.findById(5L)).thenReturn(Optional.of(chapter));

        assertThrows(Exception.class, () -> service.submitForReview(5L, 7L));

        verify(reviewRequestRepository, never()).save(any(ReviewRequest.class));
    }

    @Test
    void submitForReview_whenStaffNotAssignedToComic_throwsAndKeepsChapterDraft() {
        Chapter chapter = chapter(5L, 1L, ChapterStatus.DRAFT);
        when(chapterRepository.findById(5L)).thenReturn(Optional.of(chapter));
        when(assignmentRepository.existsById_ComicIdAndId_StaffId(1L, 7L)).thenReturn(false);

        assertThrows(Exception.class, () -> service.submitForReview(5L, 7L));

        assertEquals(ChapterStatus.DRAFT, chapter.getStatus());
        verify(reviewRequestRepository, never()).save(any(ReviewRequest.class));
        verify(chapterRepository, never()).save(any(Chapter.class));
    }

    @Test
    void submitForReview_whenChapterAlreadyHasPendingRequest_throwsAndDoesNotSave() {
        Chapter chapter = chapter(5L, 1L, ChapterStatus.DRAFT);
        when(chapterRepository.findById(5L)).thenReturn(Optional.of(chapter));
        when(assignmentRepository.existsById_ComicIdAndId_StaffId(1L, 7L)).thenReturn(true);
        when(reviewRequestRepository.existsByChapter_IdAndStatus(5L, ReviewStatus.PENDING)).thenReturn(true);

        assertThrows(Exception.class, () -> service.submitForReview(5L, 7L));

        assertEquals(ChapterStatus.DRAFT, chapter.getStatus());
        verify(reviewRequestRepository, never()).save(any(ReviewRequest.class));
        verify(chapterRepository, never()).save(any(Chapter.class));
    }

    // ---------- approveChapter ----------

    @Test
    void approveChapter_whenPublishAtInFuture_schedulesChapter() throws Exception {
        Chapter chapter = chapter(5L, 1L, ChapterStatus.PENDING_REVIEW);
        chapter.setPublishAt(LocalDateTime.now().plusDays(1));
        when(chapterRepository.findById(5L)).thenReturn(Optional.of(chapter));

        ReviewRequest review = new ReviewRequest();
        review.setId(10L);
        review.setChapter(chapter);
        review.setSubmittedBy(7L);
        review.setStatus(ReviewStatus.PENDING);
        when(reviewRequestRepository.findByChapter_IdAndStatus(5L, ReviewStatus.PENDING)).thenReturn(Optional.of(review));

        ChapterResponse response = service.approveChapter(5L, 99L);

        assertEquals(ChapterStatus.SCHEDULED, chapter.getStatus());
        assertEquals(ChapterStatus.SCHEDULED, response.getStatus());
        assertEquals(ReviewStatus.APPROVED, review.getStatus());
        assertEquals(99L, review.getReviewedBy());

        verify(chapterRepository).save(chapter);
        verify(reviewRequestRepository).save(review);
    }

    @Test
    void approveChapter_whenPublishAtInPast_publishesImmediately() throws Exception {
        Chapter chapter = chapter(5L, 1L, ChapterStatus.PENDING_REVIEW);
        chapter.setPublishAt(LocalDateTime.now().minusMinutes(1));
        when(chapterRepository.findById(5L)).thenReturn(Optional.of(chapter));

        ReviewRequest review = new ReviewRequest();
        review.setChapter(chapter);
        review.setStatus(ReviewStatus.PENDING);
        when(reviewRequestRepository.findByChapter_IdAndStatus(5L, ReviewStatus.PENDING)).thenReturn(Optional.of(review));

        ChapterResponse response = service.approveChapter(5L, 99L);

        assertEquals(ChapterStatus.PUBLISHED, chapter.getStatus());
        assertEquals(ChapterStatus.PUBLISHED, response.getStatus());
        assertEquals(ReviewStatus.APPROVED, review.getStatus());
    }

    @Test
    void approveChapter_whenPublishAtNull_publishesImmediately() throws Exception {
        Chapter chapter = chapter(6L, 1L, ChapterStatus.PENDING_REVIEW);
        when(chapterRepository.findById(6L)).thenReturn(Optional.of(chapter));

        ReviewRequest review = new ReviewRequest();
        review.setChapter(chapter);
        review.setStatus(ReviewStatus.PENDING);
        when(reviewRequestRepository.findByChapter_IdAndStatus(6L, ReviewStatus.PENDING)).thenReturn(Optional.of(review));

        ChapterResponse response = service.approveChapter(6L, 99L);

        assertEquals(ChapterStatus.PUBLISHED, chapter.getStatus());
        assertEquals(ChapterStatus.PUBLISHED, response.getStatus());
    }

    @Test
    void approveChapter_whenNoPendingReview_throwsAndDoesNotSave() throws Exception {
        Chapter chapter = chapter(5L, 1L, ChapterStatus.PENDING_REVIEW);
        when(chapterRepository.findById(5L)).thenReturn(Optional.of(chapter));
        when(reviewRequestRepository.findByChapter_IdAndStatus(5L, ReviewStatus.PENDING)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> service.approveChapter(5L, 99L));

        verify(chapterRepository, never()).save(any(Chapter.class));
        verify(reviewRequestRepository, never()).save(any(ReviewRequest.class));
    }

    // ---------- rejectChapter ----------

    @Test
    void rejectChapter_whenValid_returnsChapterToDraftAndStoresReason() throws Exception {
        Chapter chapter = chapter(5L, 1L, ChapterStatus.PENDING_REVIEW);
        when(chapterRepository.findById(5L)).thenReturn(Optional.of(chapter));

        ReviewRequest review = new ReviewRequest();
        review.setId(10L);
        review.setChapter(chapter);
        review.setSubmittedBy(7L);
        review.setStatus(ReviewStatus.PENDING);
        when(reviewRequestRepository.findByChapter_IdAndStatus(5L, ReviewStatus.PENDING)).thenReturn(Optional.of(review));

        ChapterResponse response = service.rejectChapter(5L, 99L, "   Ảnh bị mờ  ");

        assertEquals(ChapterStatus.DRAFT, chapter.getStatus());
        assertEquals(ChapterStatus.DRAFT, response.getStatus());
        assertEquals(ReviewStatus.REJECTED, review.getStatus());
        assertEquals(99L, review.getReviewedBy());
        assertEquals("Ảnh bị mờ", review.getReason());

        verify(chapterRepository).save(chapter);
        verify(reviewRequestRepository).save(review);
    }

    @Test
    void rejectChapter_whenReasonIsBlank_throwsAndDoesNotChangeState() {
        Chapter chapter = chapter(5L, 1L, ChapterStatus.PENDING_REVIEW);
        when(chapterRepository.findById(5L)).thenReturn(Optional.of(chapter));

        ReviewRequest review = new ReviewRequest();
        review.setChapter(chapter);
        review.setStatus(ReviewStatus.PENDING);
        when(reviewRequestRepository.findByChapter_IdAndStatus(5L, ReviewStatus.PENDING)).thenReturn(Optional.of(review));

        assertThrows(Exception.class, () -> service.rejectChapter(5L, 99L, "   "));

        assertEquals(ChapterStatus.PENDING_REVIEW, chapter.getStatus());
        assertEquals(ReviewStatus.PENDING, review.getStatus());
        verify(chapterRepository, never()).save(any(Chapter.class));
        verify(reviewRequestRepository, never()).save(any(ReviewRequest.class));
    }

    @Test
    void rejectChapter_whenReasonTooLong_throwsAndDoesNotChangeState() {
        Chapter chapter = chapter(5L, 1L, ChapterStatus.PENDING_REVIEW);
        when(chapterRepository.findById(5L)).thenReturn(Optional.of(chapter));

        ReviewRequest review = new ReviewRequest();
        review.setChapter(chapter);
        review.setStatus(ReviewStatus.PENDING);
        when(reviewRequestRepository.findByChapter_IdAndStatus(5L, ReviewStatus.PENDING)).thenReturn(Optional.of(review));

        assertThrows(Exception.class, () -> service.rejectChapter(5L, 99L, "a".repeat(1001)));

        assertEquals(ReviewStatus.PENDING, review.getStatus());
        verify(reviewRequestRepository, never()).save(any(ReviewRequest.class));
    }

    // ---------- publishDueChapters ----------

    @Test
    void publishDueChapters_whenDueChaptersExist_marksThemPublishedAndReturnsIds() throws Exception {
        Chapter due1 = chapter(5L, 1L, ChapterStatus.SCHEDULED);
        Chapter due2 = chapter(9L, 1L, ChapterStatus.SCHEDULED);
        when(chapterRepository.findByStatusAndPublishAtLessThanEqual(eq(ChapterStatus.SCHEDULED), any(LocalDateTime.class)))
                .thenReturn(List.of(due1, due2));
        when(chapterRepository.save(any(Chapter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Long> publishedIds = service.publishDueChapters();

        assertEquals(List.of(5L, 9L), publishedIds);
        assertEquals(ChapterStatus.PUBLISHED, due1.getStatus());
        assertEquals(ChapterStatus.PUBLISHED, due2.getStatus());
        verify(chapterRepository, times(2)).save(any(Chapter.class));
    }

    // ---------- helpers ----------

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
}