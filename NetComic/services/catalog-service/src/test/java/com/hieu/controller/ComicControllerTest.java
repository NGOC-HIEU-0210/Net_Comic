package com.hieu.controller;

import com.hieu.enums.ComicStatus;
import com.hieu.payload.request.catalog.ComicRequest;
import com.hieu.payload.response.catalog.CategoryResponse;
import com.hieu.payload.response.catalog.ComicResponse;
import com.hieu.service.ComicService;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
class ComicControllerTest {

    private static final String BASE_URL = "/api/version-1/comics";

    @Mock
    private ComicService comicService;

    @InjectMocks
    private ComicController comicController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(comicController).build();
    }

    // ---------- create ----------

    @Test
    void createComic_returns201() throws Exception {
        when(comicService.createComic(any(ComicRequest.class)))
                .thenReturn(response(1L, "One Piece", ComicStatus.DRAFT));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"One Piece\",\"description\":\"Hải tặc\","
                                + "\"authorId\":1,\"categoryIds\":[10,11]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("One Piece"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.authorId").value(1))
                .andExpect(jsonPath("$.authorName").value("Oda"))
                .andExpect(jsonPath("$.categories[0].name").value("Hành động"));

        ArgumentCaptor<ComicRequest> captor = ArgumentCaptor.forClass(ComicRequest.class);
        verify(comicService).createComic(captor.capture());
        assertEquals("One Piece", captor.getValue().getTitle());
        assertEquals(1L, captor.getValue().getAuthorId());
        assertEquals(Set.of(10L, 11L), captor.getValue().getCategoryIds());
    }

    @Test
    void createComic_missingTitle_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"authorId\":1,\"categoryIds\":[10]}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(comicService);
    }

    @Test
    void createComic_blankTitle_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"   \",\"authorId\":1,\"categoryIds\":[10]}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(comicService);
    }

    @Test
    void createComic_missingAuthor_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"One Piece\",\"categoryIds\":[10]}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(comicService);
    }

    @Test
    void createComic_emptyCategoryIds_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"One Piece\",\"authorId\":1,\"categoryIds\":[]}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(comicService);
    }

    @Test
    void createComic_invalidStatus_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"One Piece\",\"authorId\":1,\"categoryIds\":[10],"
                                + "\"status\":\"khong-ton-tai\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(comicService);
    }

    // ---------- update / delete / get ----------

    @Test
    void updateComic_returns200() throws Exception {
        when(comicService.updateComic(eq(5L), any(ComicRequest.class)))
                .thenReturn(response(5L, "Naruto", ComicStatus.PUBLISHED));

        mockMvc.perform(put(BASE_URL + "/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Naruto\",\"authorId\":1,\"categoryIds\":[10],"
                                + "\"status\":\"PUBLISHED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.title").value("Naruto"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        ArgumentCaptor<ComicRequest> captor = ArgumentCaptor.forClass(ComicRequest.class);
        verify(comicService).updateComic(eq(5L), captor.capture());
        assertEquals(ComicStatus.PUBLISHED, captor.getValue().getStatus());
    }

    @Test
    void updateComic_invalidBody_returns400() throws Exception {
        mockMvc.perform(put(BASE_URL + "/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"authorId\":1,\"categoryIds\":[10]}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(comicService);
    }

    @Test
    void deleteComic_returnsSuccessMessage() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Đã ẩn truyện"));

        verify(comicService).deleteComic(8L);
    }

    @Test
    void getComicById_returnsBody() throws Exception {
        when(comicService.getComicById(3L)).thenReturn(response(3L, "Bleach", ComicStatus.PUBLISHED));

        mockMvc.perform(get(BASE_URL + "/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.title").value("Bleach"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    // ---------- list ----------

    @Test
    void getComics_returnsPageContent() throws Exception {
        when(comicService.searchComics(any(), any(), any(), any(), any(Pageable.class))).thenReturn(pageOf());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("One Piece"))
                .andExpect(jsonPath("$.content[1].title").value("Naruto"));
    }

    @Test
    void getComics_passesFiltersToService() throws Exception {
        when(comicService.searchComics(eq("one"), eq(ComicStatus.PUBLISHED), eq(1L), eq(10L), any(Pageable.class)))
                .thenReturn(pageOf());

        mockMvc.perform(get(BASE_URL)
                        .param("keyword", "one")
                        .param("status", "PUBLISHED")
                        .param("authorId", "1")
                        .param("categoryId", "10"))
                .andExpect(status().isOk());

        verify(comicService).searchComics(eq("one"), eq(ComicStatus.PUBLISHED), eq(1L), eq(10L),
                any(Pageable.class));
    }

    @Test
    void getComics_withoutFilters_passesNullsAndNewestFirst() throws Exception {
        when(comicService.searchComics(any(), any(), any(), any(), any(Pageable.class))).thenReturn(pageOf());

        mockMvc.perform(get(BASE_URL)).andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(comicService).searchComics(isNull(), isNull(), isNull(), isNull(), captor.capture());

        Pageable pageable = captor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("id").getDirection());
    }

    @Test
    void getComics_invalidStatus_returns400() throws Exception {
        mockMvc.perform(get(BASE_URL).param("status", "nope"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(comicService);
    }

    @Test
    void getComics_ascDirectionAndSortByTitle() throws Exception {
        when(comicService.searchComics(any(), any(), any(), any(), any(Pageable.class))).thenReturn(pageOf());

        mockMvc.perform(get(BASE_URL).param("sortBy", "title").param("sortDirection", "asc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(comicService).searchComics(any(), any(), any(), any(), captor.capture());
        assertEquals(Sort.Direction.ASC, captor.getValue().getSort().getOrderFor("title").getDirection());
    }

    @Test
    void getComics_invalidSortDirectionFallsBackToDesc() throws Exception {
        when(comicService.searchComics(any(), any(), any(), any(), any(Pageable.class))).thenReturn(pageOf());

        mockMvc.perform(get(BASE_URL).param("sortDirection", "nope").param("sortBy", "id"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(comicService).searchComics(any(), any(), any(), any(), captor.capture());
        assertEquals(Sort.Direction.DESC, captor.getValue().getSort().getOrderFor("id").getDirection());
    }

    @Test
    void getComics_capsSizeAndFallsBackOnUnknownSortField() throws Exception {
        when(comicService.searchComics(any(), any(), any(), any(), any(Pageable.class))).thenReturn(pageOf());

        mockMvc.perform(get(BASE_URL)
                        .param("page", "-3")
                        .param("size", "1000")
                        .param("sortBy", "khongTonTai"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(comicService).searchComics(any(), any(), any(), any(), captor.capture());

        Pageable pageable = captor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(100, pageable.getPageSize());
        assertNotNull(pageable.getSort().getOrderFor("id"));
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("id").getDirection());
    }

    // ---------- helpers ----------

    private static ComicResponse response(Long id, String title, ComicStatus comicStatus) {
        ComicResponse response = new ComicResponse();
        response.setId(id);
        response.setTitle(title);
        response.setStatus(comicStatus);
        response.setAuthorId(1L);
        response.setAuthorName("Oda");

        CategoryResponse category = new CategoryResponse();
        category.setId(10L);
        category.setName("Hành động");
        response.setCategories(List.of(category));
        return response;
    }

    private static PageImpl<ComicResponse> pageOf() {
        return new PageImpl<>(
                List.of(
                        response(1L, "One Piece", ComicStatus.PUBLISHED),
                        response(2L, "Naruto", ComicStatus.DRAFT)
                ),
                PageRequest.of(0, 10),
                2
        );
    }
}
