package com.hieu.controller;

import com.hieu.payload.request.catalog.CategoryRequest;
import com.hieu.payload.response.catalog.CategoryResponse;
import com.hieu.service.CategoryService;
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
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();
    }

    @Test
    void createCategory_returns201() throws Exception {
        when(categoryService.createCategory(any(CategoryRequest.class)))
                .thenReturn(response(1L, "Hành động", true, "Truyện hành động"));

        mockMvc.perform(post("/api/version-1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Hành động\",\"active\":true,\"description\":\"Truyện hành động\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Hành động"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.descrition").value("Truyện hành động"));

        ArgumentCaptor<CategoryRequest> captor = ArgumentCaptor.forClass(CategoryRequest.class);
        verify(categoryService).createCategory(captor.capture());
        assertEquals("Hành động", captor.getValue().getName());
        assertEquals(Boolean.TRUE, captor.getValue().getActive());
        assertEquals("Truyện hành động", captor.getValue().getDescription());
    }

    @Test
    void updateCategory_returns200() throws Exception {
        when(categoryService.updateCategory(any(Long.class), any(CategoryRequest.class)))
                .thenReturn(response(5L, "Romance", false, "Tình cảm"));

        mockMvc.perform(put("/api/version-1/categories/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Romance\",\"active\":false,\"description\":\"Tình cảm\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Romance"))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.descrition").value("Tình cảm"));

        verify(categoryService).updateCategory(org.mockito.ArgumentMatchers.eq(5L), any(CategoryRequest.class));
    }

    @Test
    void deleteCategory_returnsSuccessMessage() throws Exception {
        mockMvc.perform(delete("/api/version-1/categories/8"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Xóa thành công")));

        verify(categoryService).deleteCategory(8L);
    }

    @Test
    void getCategoryById_returnsBody() throws Exception {
        when(categoryService.getCategoryById(3L)).thenReturn(response(3L, "Comedy", true, "Hài"));

        mockMvc.perform(get("/api/version-1/categories/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Comedy"))
                .andExpect(jsonPath("$.descrition").value("Hài"));
    }



    @Test
    void getAllCategories_invalidSortDirectionFallsBackToAsc() throws Exception {
        when(categoryService.getAllCategories(any(Pageable.class))).thenReturn(pageOf());

        mockMvc.perform(get("/api/version-1/categories").param("sortDirection", "nope").param("sortBy", "id"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(categoryService).getAllCategories(captor.capture());
        assertEquals(Sort.Direction.ASC, captor.getValue().getSort().getOrderFor("id").getDirection());
    }

    @Test
    void getActiveCategories_returnsListAndIsNotCapturedAsId() throws Exception {
        when(categoryService.getCategoryByActiveTrue())
                .thenReturn(List.of(response(9L, "Live", true, "Đang hiện")));

        mockMvc.perform(get("/api/version-1/categories/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(9))
                .andExpect(jsonPath("$[0].name").value("Live"))
                .andExpect(jsonPath("$[0].descrition").value("Đang hiện"));
    }

    @Test
    void searchActiveCategories_passesKeyword() throws Exception {
        when(categoryService.searchCategoriesByActiveTrue("act"))
                .thenReturn(List.of(response(2L, "Action", true, "Hành động")));

        mockMvc.perform(get("/api/version-1/categories/search-active").param("keyword", "act"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Action"));

        verify(categoryService).searchCategoriesByActiveTrue("act");
    }

    @Test
    void getAllCategories_capsSizeAndFallsBackOnUnknownSortField() throws Exception {
        when(categoryService.getAllCategories(any(Pageable.class)))
                .thenReturn(pageOf());

        mockMvc.perform(get("/api/version-1/categories")
                        .param("page", "-3")
                        .param("size", "1000")
                        .param("sortBy", "khongTonTai"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(categoryService).getAllCategories(captor.capture());

        Pageable pageable = captor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(100, pageable.getPageSize());

        assertNotNull(pageable.getSort().getOrderFor("name"));
        assertEquals(
                Sort.Direction.ASC,
                pageable.getSort().getOrderFor("name").getDirection()
        );
    }



    private static CategoryResponse response(Long id, String name, Boolean active, String description) {
        CategoryResponse response = new CategoryResponse();
        response.setId(id);
        response.setName(name);
        response.setActive(active);
        response.setDescrition(description);
        return response;
    }
    private static PageImpl<CategoryResponse> pageOf() {
        return new PageImpl<>(
                List.of(
                        response(1L, "Hành động", true, "Truyện hành động"),
                        response(2L, "Phiêu lưu", true, "Truyện phiêu lưu")
                ),
                PageRequest.of(0, 10),
                2
        );
    }
}