package com.hieu.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hieu.payload.request.catalog.BannerRequest;
import com.hieu.payload.response.catalog.BannerResponse;

public interface BannerService {

    BannerResponse createBanner(BannerRequest request) throws Exception;

    BannerResponse updateBanner(Long id, BannerRequest request) throws Exception;


    void deleteBanner(Long id) throws Exception;

    BannerResponse getBannerById(Long id) throws Exception;

    Page<BannerResponse> getAllBanners(Pageable pageable);


    List<BannerResponse> getActiveBanners();
}
