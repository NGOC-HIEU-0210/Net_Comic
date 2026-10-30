package com.hieu.controller;

import com.hieu.enums.ChapterStatus;
import com.hieu.enums.ReviewStatus;
import com.hieu.payload.request.catalog.ReviewApproveRequest;
import com.hieu.payload.request.catalog.ReviewRejectRequest;
import com.hieu.payload.request.catalog.ReviewSubmitRequest;
import com.hieu.payload.response.catalog.ReviewRequestResponse;
import com.hieu.service.ReviewRequestService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReviewRequestControllerTest {

    private static final String BASE = "/api/version-1/review-requests";

    @Mock
    private ReviewRequestService reviewRequestService;

    @InjectMocks
    private ReviewRequestController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ---------- submit ----------

    @Test
    void submit_returns201() throws Exception {
        when(reviewRequestService.submit(any(ReviewSubmitRequest.class)))
                .thenReturn(response(10L, ReviewStatus.PENDING, 7L));

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chapterId\":5,\"staffId\":7}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.submittedBy").value(7))
                .andExpect(jsonPath("$.chapterId").value(5));

        ArgumentCaptor<ReviewSubmitRequest> captor = ArgumentCaptor.forClass(ReviewSubmitRequest.class);
        verify(reviewRequestService).submit(captor.capture());
        assertEquals(5L, captor.getValue().getChapterId());
        assertEquals(7L, captor.getValue().getStaffId());
    }

    @Test
    void submit_whenChapterIdMissing_returns400() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"staffId\":7}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reviewRequestService);
    }

    @Test
    void submit_whenStaffIdMissing_returns400() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chapterId\":5}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reviewRequestService);
    }

    @Test
    void submit_whenBodyMalformed_returns400() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reviewRequestService);
    }

    @Test
    void submit_whenServiceThrows_propagatesException() throws Exception {
        when(reviewRequestService.submit(any(ReviewSubmitRequest.class)))
                .thenThrow(new Exception("Chương đang có yêu cầu chờ duyệt"));

        ServletException ex = assertThrows(ServletException.class, () ->
                mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chapterId\":5,\"staffId\":7}")));

        assertEquals("Chương đang có yêu cầu chờ duyệt", ex.getCause().getMessage());
    }

    // ---------- getById ----------

    @Test
    void getById_returns200() throws Exception {
        when(reviewRequestService.getById(10L)).thenReturn(response(10L, ReviewStatus.PENDING, 7L));

        mockMvc.perform(get(BASE + "/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.chapterId").value(5))
                .andExpect(jsonPath("$.chapterStatus").value("PENDING_REVIEW"))
                .andExpect(jsonPath("$.comicTitle").value("One Piece"));
    }

    @Test
    void getById_whenIdNotNumeric_returns400() throws Exception {
        mockMvc.perform(get(BASE + "/abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reviewRequestService);
    }

    @Test
    void getById_whenServiceThrows_propagatesException() throws Exception {
        when(reviewRequestService.getById(10L)).thenThrow(new Exception("Không tìm thấy"));

        assertThrows(ServletException.class, () -> mockMvc.perform(get(BASE + "/10")));
    }

    // ---------- getAll ----------

    @Test
    void getAll_withDefaults_passesNullStatusAndDefaultPaging() throws Exception {
        when(reviewRequestService.getAll(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response(10L, ReviewStatus.PENDING, 7L)), PageRequest.of(0, 10), 1));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(reviewRequestService).getAll(eq(null), captor.capture());
        Pageable pageable = captor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        Sort.Order order = pageable.getSort().getOrderFor("id");
        assertNotNull(order);
        assertTrue(order.isAscending());
    }

    @Test
    void getAll_withStatusParam_passesStatusToService() throws Exception {
        when(reviewRequestService.getAll(eq(ReviewStatus.REJECTED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response(11L, ReviewStatus.REJECTED, 7L)), PageRequest.of(0, 10), 1));

        mockMvc.perform(get(BASE).param("status", "REJECTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("REJECTED"));
    }

    @Test
    void getAll_whenStatusInvalid_returns400() throws Exception {
        mockMvc.perform(get(BASE).param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reviewRequestService);
    }

    @Test
    void getAll_clampsPagingAndAppliesDescendingSort() throws Exception {
        when(reviewRequestService.getAll(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 100), 0));

        mockMvc.perform(get(BASE)
                        .param("page", "-3")
                        .param("size", "500")
                        .param("sortBy", "submittedBy")
                        .param("sortDirection", "DESC"))
                .andExpect(status().isOk());

        Pageable pageable = captureGetAllPageable();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(100, pageable.getPageSize());
        Sort.Order order = pageable.getSort().getOrderFor("submittedBy");
        assertNotNull(order);
        assertTrue(order.isDescending());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-5"})
    void getAll_whenSizeBelowOne_clampsToOne(String size) throws Exception {
        when(reviewRequestService.getAll(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 1), 0));

        mockMvc.perform(get(BASE).param("size", size))
                .andExpect(status().isOk());

        assertEquals(1, captureGetAllPageable().getPageSize());
    }

    @ParameterizedTest
    @ValueSource(strings = {"id", "status", "submittedBy", "reviewedBy"})
    void getAll_whenSortFieldWhitelisted_usesIt(String sortBy) throws Exception {
        when(reviewRequestService.getAll(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get(BASE).param("sortBy", sortBy))
                .andExpect(status().isOk());

        assertNotNull(captureGetAllPageable().getSort().getOrderFor(sortBy));
    }

    @ParameterizedTest
    @ValueSource(strings = {"reason", "password", "chapter.title", "chapter"})
    void getAll_whenSortFieldNotWhitelisted_fallsBackToId(String sortBy) throws Exception {
        when(reviewRequestService.getAll(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get(BASE).param("sortBy", sortBy))
                .andExpect(status().isOk());

        Sort sort = captureGetAllPageable().getSort();
        assertNotNull(sort.getOrderFor("id"));
        assertNull(sort.getOrderFor(sortBy));
    }

    @Test
    void getAll_whenSortDirectionInvalid_fallsBackToAscending() throws Exception {
        when(reviewRequestService.getAll(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get(BASE).param("sortDirection", "sideways"))
                .andExpect(status().isOk());

        Sort.Order order = captureGetAllPageable().getSort().getOrderFor("id");
        assertNotNull(order);
        assertTrue(order.isAscending());
    }

    // ---------- pending ----------

    @Test
    void getPending_returns200() throws Exception {
        when(reviewRequestService.getPending(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response(10L, ReviewStatus.PENDING, 7L)), PageRequest.of(0, 10), 1));

        mockMvc.perform(get(BASE + "/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    void getPending_clampsPagingAndSanitizesSort() throws Exception {
        when(reviewRequestService.getPending(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 100), 0));

        mockMvc.perform(get(BASE + "/pending")
                        .param("page", "-1")
                        .param("size", "1000")
                        .param("sortBy", "reason")
                        .param("sortDirection", "desc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(reviewRequestService).getPending(captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(100, captor.getValue().getPageSize());
        Sort.Order order = captor.getValue().getSort().getOrderFor("id");
        assertNotNull(order);
        assertTrue(order.isDescending());
    }

    // ---------- by-chapter / by-staff ----------

    @Test
    void byChapter_returns200() throws Exception {
        when(reviewRequestService.getByChapter(eq(5L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response(10L, ReviewStatus.PENDING, 7L)), PageRequest.of(0, 10), 1));

        mockMvc.perform(get(BASE + "/by-chapter/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].chapterId").value(5));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(reviewRequestService).getByChapter(eq(5L), captor.capture());
        assertNotNull(captor.getValue().getSort().getOrderFor("id"));
    }

    @Test
    void byChapter_whenServiceThrows_propagatesException() throws Exception {
        when(reviewRequestService.getByChapter(eq(5L), any(Pageable.class)))
                .thenThrow(new Exception("Không tìm thấy chương"));

        assertThrows(ServletException.class, () -> mockMvc.perform(get(BASE + "/by-chapter/5")));
    }

    @Test
    void byStaff_returns200() throws Exception {
        when(reviewRequestService.getByStaff(eq(7L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response(10L, ReviewStatus.PENDING, 7L)), PageRequest.of(0, 10), 1));

        mockMvc.perform(get(BASE + "/by-staff/7")
                        .param("sortBy", "status")
                        .param("sortDirection", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].submittedBy").value(7));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(reviewRequestService).getByStaff(eq(7L), captor.capture());
        Sort.Order order = captor.getValue().getSort().getOrderFor("status");
        assertNotNull(order);
        assertTrue(order.isDescending());
    }

    // ---------- approve ----------

    @Test
    void approve_returns200() throws Exception {
        ReviewRequestResponse approved = response(10L, ReviewStatus.APPROVED, 7L);
        approved.setReviewedBy(99L);
        approved.setChapterStatus(ChapterStatus.PUBLISHED);
        when(reviewRequestService.approve(eq(10L), any(ReviewApproveRequest.class))).thenReturn(approved);

        mockMvc.perform(post(BASE + "/10/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"adminId\":99}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.reviewedBy").value(99))
                .andExpect(jsonPath("$.chapterStatus").value("PUBLISHED"));

        ArgumentCaptor<ReviewApproveRequest> captor = ArgumentCaptor.forClass(ReviewApproveRequest.class);
        verify(reviewRequestService).approve(eq(10L), captor.capture());
        assertEquals(99L, captor.getValue().getAdminId());
    }

    @Test
    void approve_whenAdminIdMissing_returns400() throws Exception {
        mockMvc.perform(post(BASE + "/10/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reviewRequestService);
    }

    @Test
    void approve_whenServiceThrows_propagatesException() throws Exception {
        when(reviewRequestService.approve(eq(10L), any(ReviewApproveRequest.class)))
                .thenThrow(new Exception("Yêu cầu duyệt đã được xử lý"));

        assertThrows(ServletException.class, () ->
                mockMvc.perform(post(BASE + "/10/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"adminId\":99}")));
    }

    // ---------- reject ----------

    @Test
    void reject_returns200() throws Exception {
        ReviewRequestResponse rejected = response(10L, ReviewStatus.REJECTED, 7L);
        rejected.setReviewedBy(99L);
        rejected.setReason("Ảnh bị mờ");
        rejected.setChapterStatus(ChapterStatus.DRAFT);
        when(reviewRequestService.reject(eq(10L), any(ReviewRejectRequest.class))).thenReturn(rejected);

        mockMvc.perform(post(BASE + "/10/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"adminId\":99,\"reason\":\"Ảnh bị mờ\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.reason").value("Ảnh bị mờ"))
                .andExpect(jsonPath("$.chapterStatus").value("DRAFT"));

        ArgumentCaptor<ReviewRejectRequest> captor = ArgumentCaptor.forClass(ReviewRejectRequest.class);
        verify(reviewRequestService).reject(eq(10L), captor.capture());
        assertEquals(99L, captor.getValue().getAdminId());
        assertEquals("Ảnh bị mờ", captor.getValue().getReason());
    }

    @Test
    void reject_whenAdminIdMissing_returns400() throws Exception {
        mockMvc.perform(post(BASE + "/10/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"lý do\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reviewRequestService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"adminId\":99}", "{\"adminId\":99,\"reason\":\"\"}", "{\"adminId\":99,\"reason\":\"   \"}"})
    void reject_whenReasonMissingOrBlank_returns400(String body) throws Exception {
        mockMvc.perform(post(BASE + "/10/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reviewRequestService);
    }

    @Test
    void reject_whenReasonTooLong_returns400() throws Exception {
        String body = "{\"adminId\":99,\"reason\":\"" + "a".repeat(1001) + "\"}";

        mockMvc.perform(post(BASE + "/10/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reviewRequestService);
    }

    @Test
    void reject_whenReasonIsExactlyMaxLength_returns200() throws Exception {
        when(reviewRequestService.reject(eq(10L), any(ReviewRejectRequest.class)))
                .thenReturn(response(10L, ReviewStatus.REJECTED, 7L));
        String body = "{\"adminId\":99,\"reason\":\"" + "a".repeat(1000) + "\"}";

        mockMvc.perform(post(BASE + "/10/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    // ---------- cancel ----------

    @Test
    void cancel_returns200WithMessage() throws Exception {
        mockMvc.perform(delete(BASE + "/10").param("staffId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hủy yêu cầu duyệt thành công"));

        verify(reviewRequestService).cancel(10L, 7L);
    }

    @Test
    void cancel_whenStaffIdMissing_returns400() throws Exception {
        mockMvc.perform(delete(BASE + "/10"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reviewRequestService);
    }

    @Test
    void cancel_whenServiceThrows_propagatesException() throws Exception {
        doThrow(new Exception("Chỉ nhân viên đã gửi yêu cầu mới được hủy"))
                .when(reviewRequestService).cancel(10L, 8L);

        assertThrows(ServletException.class, () ->
                mockMvc.perform(delete(BASE + "/10").param("staffId", "8")));
    }

    // ---------- helpers ----------

    private Pageable captureGetAllPageable() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(reviewRequestService).getAll(any(), captor.capture());
        return captor.getValue();
    }

    private static ReviewRequestResponse response(Long id, ReviewStatus status, Long submittedBy) {
        ReviewRequestResponse response = new ReviewRequestResponse();
        response.setId(id);
        response.setChapterId(5L);
        response.setChapterNo(1);
        response.setChapterTitle("Chapter 1");
        response.setChapterStatus(ChapterStatus.PENDING_REVIEW);
        response.setComicId(1L);
        response.setComicTitle("One Piece");
        response.setSubmittedBy(submittedBy);
        response.setStatus(status);
        return response;
    }
}