package com.hieu.controller;

import com.hieu.payload.request.catalog.AuthorRequest;
import com.hieu.payload.response.catalog.AuthorResponse;
import com.hieu.service.AuthorService;
import org.hamcrest.Matchers;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthorControllerTest {

    @Mock
    private AuthorService authorService;

    @InjectMocks
    private AuthorController authorController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authorController).build();
    }

    @Test
    void createAuthor_returns201() throws Exception {
        when(authorService.createAuthor(any(AuthorRequest.class)))
                .thenReturn(response(1L, "Oda Eiichiro", true, "Tác giả One Piece"));

        mockMvc.perform(post("/api/version-1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Oda Eiichiro\",\"active\":true,\"bio\":\"Tác giả One Piece\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Oda Eiichiro"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.bio").value("Tác giả One Piece"));

        ArgumentCaptor<AuthorRequest> captor = ArgumentCaptor.forClass(AuthorRequest.class);
        verify(authorService).createAuthor(captor.capture());
        assertEquals("Oda Eiichiro", captor.getValue().getName());
        assertEquals(Boolean.TRUE, captor.getValue().getActive());
        assertEquals("Tác giả One Piece", captor.getValue().getBio());
    }

    @Test
    void updateAuthor_returns200() throws Exception {
        when(authorService.updateAuthor(any(Long.class), any(AuthorRequest.class)))
                .thenReturn(response(5L, "Gosho Aoyama", false, "Tác giả Conan"));

        mockMvc.perform(put("/api/version-1/authors/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Gosho Aoyama\",\"active\":false,\"bio\":\"Tác giả Conan\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Gosho Aoyama"))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.bio").value("Tác giả Conan"));

        verify(authorService).updateAuthor(eq(5L), any(AuthorRequest.class));
    }

    @Test
    void deleteAuthor_returnsSuccessMessage() throws Exception {
        mockMvc.perform(delete("/api/version-1/authors/8"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Xóa thành công")));

        verify(authorService).deleteAuthor(8L);
    }

    @Test
    void getAuthorById_returnsBody() throws Exception {
        when(authorService.getAuthorById(3L)).thenReturn(response(3L, "Kishimoto", true, "Tác giả Naruto"));

        mockMvc.perform(get("/api/version-1/authors/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Kishimoto"))
                .andExpect(jsonPath("$.bio").value("Tác giả Naruto"));
    }

    @Test
    void getAllAuthors_invalidSortDirectionFallsBackToAsc() throws Exception {
        when(authorService.getAllAuthors(any(Pageable.class))).thenReturn(pageOf());

        mockMvc.perform(get("/api/version-1/authors").param("sortDirection", "nope").param("sortBy", "id"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(authorService).getAllAuthors(captor.capture());
        assertEquals(Sort.Direction.ASC, captor.getValue().getSort().getOrderFor("id").getDirection());
    }

    @Test
    void getActiveAuthors_returnsListAndIsNotCapturedAsId() throws Exception {
        when(authorService.getAuthorByActiveTrue())
                .thenReturn(List.of(response(9L, "Tô Hoài", true, "Đang hiện")));

        mockMvc.perform(get("/api/version-1/authors/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(9))
                .andExpect(jsonPath("$[0].name").value("Tô Hoài"))
                .andExpect(jsonPath("$[0].bio").value("Đang hiện"));
    }

    @Test
    void searchActiveAuthors_passesKeyword() throws Exception {
        when(authorService.searchAuthorByActiveTrue("oda"))
                .thenReturn(List.of(response(2L, "Oda Eiichiro", true, "One Piece")));

        mockMvc.perform(get("/api/version-1/authors/search-active").param("keyword", "oda"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Oda Eiichiro"));

        verify(authorService).searchAuthorByActiveTrue("oda");
    }

    @Test
    void searchAuthors_passesKeywordAndPageable() throws Exception {
        when(authorService.searchAuthors(eq("oda"), any(Pageable.class)))
                .thenReturn(pageOf());

        mockMvc.perform(get("/api/version-1/authors/search")
                        .param("keyword", "oda")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(authorService).searchAuthors(eq("oda"), captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(10, captor.getValue().getPageSize());
    }

    @Test
    void getAllAuthors_capsSizeAndFallsBackOnUnknownSortField() throws Exception {
        when(authorService.getAllAuthors(any(Pageable.class)))
                .thenReturn(pageOf());

        mockMvc.perform(get("/api/version-1/authors")
                        .param("page", "-3")
                        .param("size", "1000")
                        .param("sortBy", "khongTonTai"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        verify(authorService).getAllAuthors(captor.capture());

        Pageable pageable = captor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(100, pageable.getPageSize());

        assertNotNull(pageable.getSort().getOrderFor("name"));
        assertEquals(
                Sort.Direction.ASC,
                pageable.getSort().getOrderFor("name").getDirection()
        );
    }

    private static AuthorResponse response(Long id, String name, Boolean active, String bio) {
        AuthorResponse response = new AuthorResponse();
        response.setId(id);
        response.setName(name);
        response.setActive(active);
        response.setBio(bio);
        return response;
    }

    private static PageImpl<AuthorResponse> pageOf() {
        return new PageImpl<>(
                List.of(
                        response(1L, "Oda Eiichiro", true, "Tác giả One Piece"),
                        response(2L, "Gosho Aoyama", true, "Tác giả Conan")
                ),
                PageRequest.of(0, 10),
                2
        );
    }
}