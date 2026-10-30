package com.hieu.mapper;

import com.hieu.model.Banner;
import com.hieu.payload.request.catalog.BannerRequest;
import com.hieu.payload.response.catalog.BannerResponse;

public class BannerMapper {

    public static Banner toEntity(BannerRequest request) {
        if (request == null) {
            return null;
        }
        Banner banner = new Banner();
        banner.setImageUrl(request.getImageUrl().trim());
        banner.setLink(blankToNull(request.getLink()));
        banner.setActive(request.getActive() != null ? request.getActive() : Boolean.TRUE);
        return banner;
    }

    public static BannerResponse toResponse(Banner banner) {
        if (banner == null) {
            return null;
        }
        BannerResponse response = new BannerResponse();
        response.setId(banner.getId());
        response.setImageUrl(banner.getImageUrl());
        response.setLink(banner.getLink());
        response.setActive(banner.getActive());
        return response;
    }

    public static Banner updateEntity(Banner banner, BannerRequest request) {
        if (banner == null || request == null) {
            return banner;
        }
        banner.setImageUrl(request.getImageUrl().trim());
        banner.setLink(blankToNull(request.getLink()));
        if (request.getActive() != null) {
            banner.setActive(request.getActive());
        }
        return banner;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
