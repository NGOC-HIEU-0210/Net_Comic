package com.hieu.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.hieu.service.BannerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.hieu.model.Banner;
import com.hieu.payload.request.catalog.BannerRequest;
import com.hieu.payload.response.catalog.BannerResponse;
import com.hieu.repository.BannerRepository;

@ExtendWith(MockitoExtension.class)
class BannerServiceImplTest {

    @Mock
    private BannerRepository bannerRepository;

    @InjectMocks
    private BannerServiceImpl bannerService;

    // ---------- create ----------

    @Test
    void createBanner_success_trimsAndDefaultsToActive() throws Exception {
        BannerRequest request = request("  https://cdn/a.webp  ", "  /comics/1  ", null);
        when(bannerRepository.save(any(Banner.class))).thenAnswer(inv -> {
            Banner b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });

        BannerResponse response = bannerService.createBanner(request);

        assertEquals(1L, response.getId());
        assertEquals("https://cdn/a.webp", response.getImageUrl());
        assertEquals("/comics/1", response.getLink());
        assertTrue(response.getActive());
    }

    @Test
    void createBanner_blankLinkBecomesNull() throws Exception {
        BannerRequest request = request("https://cdn/a.webp", "   ", null);
        when(bannerRepository.save(any(Banner.class))).thenAnswer(inv -> inv.getArgument(0));

        BannerResponse response = bannerService.createBanner(request);

        assertNull(response.getLink());
    }

    @Test
    void createBanner_usesActiveFromRequest() throws Exception {
        BannerRequest request = request("https://cdn/a.webp", null, false);
        when(bannerRepository.save(any(Banner.class))).thenAnswer(inv -> inv.getArgument(0));

        BannerResponse response = bannerService.createBanner(request);

        assertFalse(response.getActive());
    }

    // ---------- update ----------

    @Test
    void updateBanner_success_keepsActiveWhenNull() throws Exception {
        Banner banner = banner(5L, "https://cdn/old.webp", "/old", true);
        BannerRequest request = request("https://cdn/new.webp", "/new", null);

        when(bannerRepository.findById(5L)).thenReturn(Optional.of(banner));
        when(bannerRepository.save(any(Banner.class))).thenAnswer(inv -> inv.getArgument(0));

        BannerResponse response = bannerService.updateBanner(5L, request);

        assertEquals("https://cdn/new.webp", response.getImageUrl());
        assertEquals("/new", response.getLink());
        assertTrue(response.getActive());
    }

    @Test
    void updateBanner_canDeactivate() throws Exception {
        Banner banner = banner(5L, "https://cdn/a.webp", null, true);
        when(bannerRepository.findById(5L)).thenReturn(Optional.of(banner));
        when(bannerRepository.save(any(Banner.class))).thenAnswer(inv -> inv.getArgument(0));

        BannerResponse response = bannerService.updateBanner(5L, request("https://cdn/a.webp", null, false));

        assertFalse(response.getActive());
    }

    @Test
    void updateBanner_notFound_throws() {
        when(bannerRepository.findById(9L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(Exception.class,
                () -> bannerService.updateBanner(9L, request("https://cdn/a.webp", null, null)));

        assertEquals("Không tìm thấy banner với id = 9", ex.getMessage());
        verify(bannerRepository, never()).save(any());
    }

    // ---------- delete ----------

    @Test
    void deleteBanner_setsActiveFalse_andDoesNotDeleteRow() throws Exception {
        Banner banner = banner(5L, "https://cdn/a.webp", null, true);
        when(bannerRepository.findById(5L)).thenReturn(Optional.of(banner));

        bannerService.deleteBanner(5L);

        assertFalse(banner.getActive());
        verify(bannerRepository).save(banner);
        verify(bannerRepository, never()).delete(any(Banner.class));
    }

    @Test
    void deleteBanner_notFound_throws() {
        when(bannerRepository.findById(9L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(Exception.class, () -> bannerService.deleteBanner(9L));

        assertEquals("Không tìm thấy banner với id = 9", ex.getMessage());
    }

    // ---------- get ----------

    @Test
    void getBannerById_success() throws Exception {
        when(bannerRepository.findById(5L))
                .thenReturn(Optional.of(banner(5L, "https://cdn/a.webp", "/x", true)));

        BannerResponse response = bannerService.getBannerById(5L);

        assertEquals(5L, response.getId());
        assertEquals("/x", response.getLink());
    }

    @Test
    void getBannerById_notFound_throws() {
        when(bannerRepository.findById(9L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(Exception.class, () -> bannerService.getBannerById(9L));

        assertEquals("Không tìm thấy banner với id = 9", ex.getMessage());
    }

    @Test
    void getAllBanners_mapsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(bannerRepository.findAll(pageable)).thenReturn(new PageImpl<>(
                List.of(banner(1L, "https://cdn/a.webp", null, true), banner(2L, "https://cdn/b.webp", null, false)),
                pageable, 2));

        Page<BannerResponse> result = bannerService.getAllBanners(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("https://cdn/b.webp", result.getContent().get(1).getImageUrl());
    }

    @Test
    void getActiveBanners_mapsList() {
        when(bannerRepository.findByActiveTrueOrderByIdDesc())
                .thenReturn(List.of(banner(2L, "https://cdn/b.webp", null, true)));

        List<BannerResponse> result = bannerService.getActiveBanners();

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
    }

    // ---------- helpers ----------

    private static BannerRequest request(String imageUrl, String link, Boolean active) {
        BannerRequest request = new BannerRequest();
        request.setImageUrl(imageUrl);
        request.setLink(link);
        request.setActive(active);
        return request;
    }

    private static Banner banner(Long id, String imageUrl, String link, boolean active) {
        Banner b = new Banner();
        b.setId(id);
        b.setImageUrl(imageUrl);
        b.setLink(link);
        b.setActive(active);
        return b;
    }
}
