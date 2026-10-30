package com.hieu.controller;

import com.hieu.payload.request.catalog.ComicAssignmentRequest;
import com.hieu.payload.request.catalog.ReassignStaffRequest;
import com.hieu.payload.response.catalog.ComicAssignmentResponse;
import com.hieu.service.ComicAssignmentService;
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
class ComicAssignmentControllerTest {

    private static final String BASE = "/api/version-1/comic-assignments";

    @Mock
    private ComicAssignmentService assignmentService;

    @InjectMocks
    private ComicAssignmentController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void create_returns201() throws Exception {
        when(assignmentService.createComicAssignment(any(ComicAssignmentRequest.class)))
                .thenReturn(response(1L, "One Piece", 7L));

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comicId\":1,\"staffId\":7}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comicId").value(1))
                .andExpect(jsonPath("$.comicTitle").value("One Piece"))
                .andExpect(jsonPath("$.staffId").value(7));

        ArgumentCaptor<ComicAssignmentRequest> captor = ArgumentCaptor.forClass(ComicAssignmentRequest.class);
        verify(assignmentService).createComicAssignment(captor.capture());
        assertEquals(1L, captor.getValue().getComicId());
        assertEquals(7L, captor.getValue().getStaffId());
    }

    @Test
    void create_whenStaffIdMissing_returns400() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comicId\":1}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(assignmentService);
    }

    @Test
    void update_returns200() throws Exception {
        when(assignmentService.updateComicAssignment(eq(1L), eq(7L), any(ReassignStaffRequest.class)))
                .thenReturn(response(1L, "One Piece", 9L));

        mockMvc.perform(put(BASE + "/1/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newStaffId\":9}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.staffId").value(9));

        ArgumentCaptor<ReassignStaffRequest> captor = ArgumentCaptor.forClass(ReassignStaffRequest.class);
        verify(assignmentService).updateComicAssignment(eq(1L), eq(7L), captor.capture());
        assertEquals(9L, captor.getValue().getNewStaffId());
    }

    @Test
    void update_whenNewStaffIdMissing_returns400() throws Exception {
        mockMvc.perform(put(BASE + "/1/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(assignmentService);
    }

    @Test
    void delete_returns200() throws Exception {
        mockMvc.perform(delete(BASE + "/1/7"))
                .andExpect(status().isOk());

        verify(assignmentService).deleteComicAssignment(1L, 7L);
    }

    @Test
    void getOne_returns200() throws Exception {
        when(assignmentService.getComicAssignment(1L, 7L)).thenReturn(response(1L, "One Piece", 7L));

        mockMvc.perform(get(BASE + "/1/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comicId").value(1))
                .andExpect(jsonPath("$.staffId").value(7));
    }

    @Test
    void getAll_clampsPagingAndMapsSortField() throws Exception {
        when(assignmentService.getAllComicAssignments(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response(1L, "One Piece", 7L)), PageRequest.of(1, 100), 101));

        mockMvc.perform(get(BASE)
                        .param("page", "-3")
                        .param("size", "500")
                        .param("sortBy", "staffId")
                        .param("sortDirection", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].staffId").value(7));

        Pageable pageable = capturePageable();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(100, pageable.getPageSize());
        Sort.Order order = pageable.getSort().getOrderFor("id.staffId");
        assertNotNull(order);
        assertTrue(order.isDescending());
    }

    @Test
    void getAll_whenSortFieldInvalid_fallsBackToComicId() throws Exception {
        when(assignmentService.getAllComicAssignments(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get(BASE).param("sortBy", "password"))
                .andExpect(status().isOk());

        Sort.Order order = capturePageable().getSort().getOrderFor("id.comicId");
        assertNotNull(order);
        assertTrue(order.isAscending());
    }

    @Test
    void byComic_usesStaffIdAsDefaultSort() throws Exception {
        when(assignmentService.getAssignmentsByComic(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response(1L, "One Piece", 7L)), PageRequest.of(0, 10), 1));

        mockMvc.perform(get(BASE + "/by-comic/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].staffId").value(7));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(assignmentService).getAssignmentsByComic(eq(1L), captor.capture());
        assertNotNull(captor.getValue().getSort().getOrderFor("id.staffId"));
    }

    @Test
    void byStaff_usesComicIdAsDefaultSort() throws Exception {
        when(assignmentService.getAssignmentsByStaff(eq(7L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response(1L, "One Piece", 7L)), PageRequest.of(0, 10), 1));

        mockMvc.perform(get(BASE + "/by-staff/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].comicId").value(1));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(assignmentService).getAssignmentsByStaff(eq(7L), captor.capture());
        assertNotNull(captor.getValue().getSort().getOrderFor("id.comicId"));
    }

    private Pageable capturePageable() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(assignmentService).getAllComicAssignments(captor.capture());
        return captor.getValue();
    }

    private static ComicAssignmentResponse response(Long comicId, String title, Long staffId) {
        ComicAssignmentResponse response = new ComicAssignmentResponse();
        response.setComicId(comicId);
        response.setComicTitle(title);
        response.setStaffId(staffId);
        return response;
    }
}
