package com.hieu.controller;

import com.hieu.enums.ChapterStatus;
import com.hieu.payload.request.catalog.ChapterRequest;
import com.hieu.payload.response.catalog.ChapterResponse;
import com.hieu.service.ChapterService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ChapterControllerTest {

    private static final String BASE = "/api/version-1/chapters";

    @Mock
    private ChapterService chapterService;

    @InjectMocks
    private ChapterController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ---------- create ----------

    @Test
    void create_returns201() throws Exception {
        when(chapterService.createChapter(eq(1L), any(ChapterRequest.class))).thenReturn(response());

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comicId\":1,\"chapterNo\":1,\"priceCoins\":0,\"status\":\"DRAFT\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.comicId").value(1))
                .andExpect(jsonPath("$.comicTitle").value("One Piece"))
                .andExpect(jsonPath("$.chapterNo").value(1))
                .andExpect(jsonPath("$.priceCoins").value(0))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        ArgumentCaptor<ChapterRequest> captor = ArgumentCaptor.forClass(ChapterRequest.class);
        verify(chapterService).createChapter(eq(1L), captor.capture());
        assertEquals(1L, captor.getValue().getComicId());
        assertEquals(1, captor.getValue().getChapterNo());
    }

    @Test
    void create_whenComicIdMissing_returns400() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chapterNo\":1,\"priceCoins\":0,\"status\":\"DRAFT\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(chapterService);
    }

    @Test
    void create_whenServiceThrows_propagatesException() throws Exception {
        when(chapterService.createChapter(eq(1L), any(ChapterRequest.class)))
                .thenThrow(new Exception("Truyện đã có chương số 1"));

        ServletException ex = assertThrows(ServletException.class, () ->
                mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comicId\":1,\"chapterNo\":1}")));

        assertEquals("Truyện đã có chương số 1", ex.getCause().getMessage());
    }

    // ---------- update ----------

    @Test
    void update_returns200() throws Exception {
        when(chapterService.updateChapter(eq(5L), any(ChapterRequest.class))).thenReturn(response());

        mockMvc.perform(put(BASE + "/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comicId\":1,\"chapterNo\":1,\"priceCoins\":0,\"status\":\"DRAFT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.chapterNo").value(1));

        ArgumentCaptor<ChapterRequest> captor = ArgumentCaptor.forClass(ChapterRequest.class);
        verify(chapterService).updateChapter(eq(5L), captor.capture());
        assertEquals(1L, captor.getValue().getComicId());
        assertEquals(1, captor.getValue().getChapterNo());
    }

    @Test
    void update_whenComicIdMissing_returns400() throws Exception {
        mockMvc.perform(put(BASE + "/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chapterNo\":1,\"priceCoins\":0,\"status\":\"DRAFT\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(chapterService);
    }

    // ---------- delete ----------

    @Test
    void delete_returns200WithMessage() throws Exception {
        mockMvc.perform(delete(BASE + "/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Xóa chương thành công"));

        verify(chapterService).deleteChapter(5L);
    }

    // ---------- getById ----------

    @Test
    void getById_returns200() throws Exception {
        when(chapterService.getChapterById(5L)).thenReturn(response());

        mockMvc.perform(get(BASE + "/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.comicId").value(1))
                .andExpect(jsonPath("$.comicTitle").value("One Piece"))
                .andExpect(jsonPath("$.chapterNo").value(1))
                .andExpect(jsonPath("$.title").value("Chapter 1"))
                .andExpect(jsonPath("$.priceCoins").value(0))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        verify(chapterService).getChapterById(5L);
    }

    @Test
    void getById_whenIdNotNumeric_returns400() throws Exception {
        mockMvc.perform(get(BASE + "/abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(chapterService);
    }

    // ---------- getChaptersByComic ----------

    @Test
    void getChaptersByComic_withDefaults_returns200() throws Exception {
        when(chapterService.getChaptersByComic(eq(1L), eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get(BASE).param("comicId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(chapterService).getChaptersByComic(eq(1L), eq(null), captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(10, captor.getValue().getPageSize());
        Sort.Order order = captor.getValue().getSort().getOrderFor("id");
        assertNotNull(order);
        assertTrue(order.isAscending());
    }

    @Test
    void getChaptersByComic_withStatusParam_passesStatusToService() throws Exception {
        when(chapterService.getChaptersByComic(eq(1L), eq(ChapterStatus.DRAFT), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get(BASE)
                        .param("comicId", "1")
                        .param("status", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("DRAFT"));

        verify(chapterService).getChaptersByComic(eq(1L), eq(ChapterStatus.DRAFT), any(Pageable.class));
    }

    @Test
    void getChaptersByComic_whenStatusInvalid_returns400() throws Exception {
        mockMvc.perform(get(BASE).param("comicId", "1").param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(chapterService);
    }

    @Test
    void getChaptersByComic_clampsPagingAndAppliesSort() throws Exception {
        when(chapterService.getChaptersByComic(eq(1L), eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 100), 0));

        mockMvc.perform(get(BASE)
                        .param("comicId", "1")
                        .param("page", "-3")
                        .param("size", "500")
                        .param("sortBy", "chapterNo")
                        .param("sortDirection", "DESC"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(chapterService).getChaptersByComic(eq(1L), eq(null), captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(100, captor.getValue().getPageSize());
        Sort.Order order = captor.getValue().getSort().getOrderFor("chapterNo");
        assertNotNull(order);
        assertTrue(order.isDescending());
    }

    // ---------- submitForReview ----------

    @Test
    void submitForReview_returns200() throws Exception {
        when(chapterService.submitForReview(5L, 7L)).thenReturn(response());

        mockMvc.perform(post(BASE + "/5/submit").param("staffId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(chapterService).submitForReview(5L, 7L);
    }

    @Test
    void submitForReview_whenStaffIdMissing_returns400() throws Exception {
        mockMvc.perform(post(BASE + "/5/submit"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(chapterService);
    }

    // ---------- approveChapter ----------

    @Test
    void approveChapter_returns200() throws Exception {
        when(chapterService.approveChapter(5L, 99L)).thenReturn(response());

        mockMvc.perform(post(BASE + "/5/approve").param("adminId", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(chapterService).approveChapter(5L, 99L);
    }

    @Test
    void approveChapter_whenAdminIdMissing_returns400() throws Exception {
        mockMvc.perform(post(BASE + "/5/approve"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(chapterService);
    }

    // ---------- rejectChapter ----------

    @Test
    void rejectChapter_returns200() throws Exception {
        when(chapterService.rejectChapter(5L, 99L, "Ảnh bị mờ")).thenReturn(response());

        mockMvc.perform(post(BASE + "/5/reject")
                        .param("adminId", "99")
                        .param("reason", "Ảnh bị mờ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(chapterService).rejectChapter(5L, 99L, "Ảnh bị mờ");
    }

    @Test
    void rejectChapter_whenAdminIdMissing_returns400() throws Exception {
        mockMvc.perform(post(BASE + "/5/reject").param("reason", "lý do"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(chapterService);
    }

    @Test
    void rejectChapter_whenReasonMissing_returns400() throws Exception {
        mockMvc.perform(post(BASE + "/5/reject").param("adminId", "99"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(chapterService);
    }

    // ---------- helpers ----------

    private static ChapterResponse response() {
        ChapterResponse response = new ChapterResponse();
        response.setId(10L);
        response.setComicId(1L);
        response.setComicTitle("One Piece");
        response.setChapterNo(1);
        response.setTitle("Chapter 1");
        response.setPriceCoins(0);
        response.setStatus(ChapterStatus.DRAFT);
        return response;
    }
}