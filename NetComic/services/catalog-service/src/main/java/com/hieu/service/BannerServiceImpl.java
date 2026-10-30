package com.hieu.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hieu.mapper.BannerMapper;
import com.hieu.model.Banner;
import com.hieu.payload.request.catalog.BannerRequest;
import com.hieu.payload.response.catalog.BannerResponse;
import com.hieu.repository.BannerRepository;
import com.hieu.service.BannerService;

@Service
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;

    public BannerServiceImpl(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BannerResponse createBanner(BannerRequest request) throws Exception {
        Banner banner = BannerMapper.toEntity(request);
        Banner saved = bannerRepository.save(banner);
        return BannerMapper.toResponse(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BannerResponse updateBanner(Long id, BannerRequest request) throws Exception {
        Banner banner = findBanner(id);
        BannerMapper.updateEntity(banner, request);
        Banner saved = bannerRepository.save(banner);
        return BannerMapper.toResponse(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBanner(Long id) throws Exception {
        Banner banner = findBanner(id);
        banner.setActive(Boolean.FALSE);
        bannerRepository.save(banner);
    }

    @Override
    @Transactional(readOnly = true)
    public BannerResponse getBannerById(Long id) throws Exception {
        return BannerMapper.toResponse(findBanner(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BannerResponse> getAllBanners(Pageable pageable) {
        return bannerRepository.findAll(pageable).map(BannerMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BannerResponse> getActiveBanners() {
        return bannerRepository.findByActiveTrueOrderByIdDesc()
                .stream()
                .map(BannerMapper::toResponse)
                .toList();
    }

    private Banner findBanner(Long id) throws Exception {
        return bannerRepository.findById(id).orElseThrow(
                () -> new Exception("Không tìm thấy banner với id = " + id));
    }
}
