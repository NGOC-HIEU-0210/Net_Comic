package com.hieu.controller;

import com.hieu.payload.request.catalog.BannerRequest;
import com.hieu.payload.response.catalog.BannerResponse;
import com.hieu.service.BannerService;
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
import static org.junit.jupiter.api.Assertions.assertNull;
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
class BannerControllerTest {

    private static final String BASE_URL = "/api/version-1/banners";

    @Mock
    private BannerService bannerService;

    @InjectMocks
    private BannerController bannerController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bannerController).build();
    }

    // ---------- create ----------

    @Test
    void createBanner_returns201() throws Exception {
        when(bannerService.createBanner(any(BannerRequest.class)))
                .thenReturn(response(1L, "https://cdn/a.webp", "/comics/1", true));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imageUrl\":\"https://cdn/a.webp\",\"link\":\"/comics/1\",\"active\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.imageUrl").value("https://cdn/a.webp"))
                .andExpect(jsonPath("$.link").value("/comics/1"))
                .andExpect(jsonPath("$.active").value(true));

        ArgumentCaptor<BannerRequest> captor = ArgumentCaptor.forClass(BannerRequest.class);
        verify(bannerService).createBanner(captor.capture());
        assertEquals("https://cdn/a.webp", captor.getValue().getImageUrl());
        assertEquals("/comics/1", captor.getValue().getLink());
        assertEquals(Boolean.TRUE, captor.getValue().getActive());
    }

    @Test
    void createBanner_onlyImageUrl_returns201() throws Exception {
        when(bannerService.createBanner(any(BannerRequest.class)))
                .thenReturn(response(2L, "https://cdn/b.webp", null, true));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imageUrl\":\"https://cdn/b.webp\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2));

        ArgumentCaptor<BannerRequest> captor = ArgumentCaptor.forClass(BannerRequest.class);
        verify(bannerService).createBanner(captor.capture());
        assertNull(captor.getValue().getLink());
        assertNull(captor.getValue().getActive());
    }

    @Test
    void createBanner_missingImageUrl_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"/comics/1\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bannerService);
    }

    @Test
    void createBanner_blankImageUrl_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imageUrl\":\"   \"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bannerService);
    }

    @Test
    void createBanner_linkTooLong_returns400() throws Exception {
        String longLink = "a".repeat(513);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imageUrl\":\"https://cdn/a.webp\",\"link\":\"" + longLink + "\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bannerService);
    }

    // ---------- update / delete / get ----------

    @Test
    void updateBanner_returns200() throws Exception {
        when(bannerService.updateBanner(eq(5L), any(BannerRequest.class)))
                .thenReturn(response(5L, "https://cdn/new.webp", "/new", false));

        mockMvc.perform(put(BASE_URL + "/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imageUrl\":\"https://cdn/new.webp\",\"link\":\"/new\",\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.imageUrl").value("https://cdn/new.webp"))
                .andExpect(jsonPath("$.active").value(false));

        verify(bannerService).updateBanner(eq(5L), any(BannerRequest.class));
    }

    @Test
    void updateBanner_invalidBody_returns400() throws Exception {
        mockMvc.perform(put(BASE_URL + "/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"/new\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bannerService);
    }

    @Test
    void deleteBanner_returnsSuccessMessage() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Xóa thành công"));

        verify(bannerService).deleteBanner(8L);
    }

    @Test
    void getBannerById_returnsBody() throws Exception {
        when(bannerService.getBannerById(3L)).thenReturn(response(3L, "https://cdn/c.webp", "/c", true));

        mockMvc.perform(get(BASE_URL + "/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.imageUrl").value("https://cdn/c.webp"))
                .andExpect(jsonPath("$.link").value("/c"));
    }

    // ---------- list ----------

    @Test
    void getAllBanners_returnsPageContent() throws Exception {
        when(bannerService.getAllBanners(any(Pageable.class))).thenReturn(pageOf());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].imageUrl").value("https://cdn/a.webp"))
                .andExpect(jsonPath("$.content[1].imageUrl").value("https://cdn/b.webp"));
    }

    @Test
    void getAllBanners_defaultsToNewestFirst() throws Exception {
        when(bannerService.getAllBanners(any(Pageable.class))).thenReturn(pageOf());

        mockMvc.perform(get(BASE_URL)).andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(bannerService).getAllBanners(captor.capture());

        Pageable pageable = captor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("id").getDirection());
    }

    @Test
    void getAllBanners_ascDirectionAndSortByActive() throws Exception {
        when(bannerService.getAllBanners(any(Pageable.class))).thenReturn(pageOf());

        mockMvc.perform(get(BASE_URL).param("sortBy", "active").param("sortDirection", "asc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(bannerService).getAllBanners(captor.capture());
        assertEquals(Sort.Direction.ASC, captor.getValue().getSort().getOrderFor("active").getDirection());
    }

    @Test
    void getAllBanners_invalidSortDirectionFallsBackToDesc() throws Exception {
        when(bannerService.getAllBanners(any(Pageable.class))).thenReturn(pageOf());

        mockMvc.perform(get(BASE_URL).param("sortDirection", "nope").param("sortBy", "id"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(bannerService).getAllBanners(captor.capture());
        assertEquals(Sort.Direction.DESC, captor.getValue().getSort().getOrderFor("id").getDirection());
    }

    @Test
    void getAllBanners_capsSizeAndFallsBackOnUnknownSortField() throws Exception {
        when(bannerService.getAllBanners(any(Pageable.class))).thenReturn(pageOf());

        mockMvc.perform(get(BASE_URL)
                        .param("page", "-3")
                        .param("size", "1000")
                        .param("sortBy", "khongTonTai"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(bannerService).getAllBanners(captor.capture());

        Pageable pageable = captor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(100, pageable.getPageSize());
        assertNotNull(pageable.getSort().getOrderFor("id"));
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("id").getDirection());
    }

    @Test
    void getActiveBanners_returnsListAndIsNotCapturedAsId() throws Exception {
        when(bannerService.getActiveBanners())
                .thenReturn(List.of(response(9L, "https://cdn/live.webp", "/live", true)));

        mockMvc.perform(get(BASE_URL + "/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(9))
                .andExpect(jsonPath("$[0].imageUrl").value("https://cdn/live.webp"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    // ---------- helpers ----------

    private static BannerResponse response(Long id, String imageUrl, String link, Boolean active) {
        BannerResponse response = new BannerResponse();
        response.setId(id);
        response.setImageUrl(imageUrl);
        response.setLink(link);
        response.setActive(active);
        return response;
    }

    private static PageImpl<BannerResponse> pageOf() {
        return new PageImpl<>(
                List.of(
                        response(1L, "https://cdn/a.webp", "/a", true),
                        response(2L, "https://cdn/b.webp", "/b", false)
                ),
                PageRequest.of(0, 10),
                2
        );
    }
}
