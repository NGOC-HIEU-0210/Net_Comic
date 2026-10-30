package com.hieu.controller;

import com.hieu.payload.request.catalog.BannerRequest;
import com.hieu.payload.response.ApiResponse;
import com.hieu.payload.response.catalog.BannerResponse;
import com.hieu.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Tag(name = "Banners", description = "Quản lý banner hiển thị trên màn hình chủ")
@RestController
@RequestMapping("/api/version-1/banners")
public class BannerController {

    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "active");
    private static final int MAX_PAGE_SIZE = 100;

    @Operation(summary = "Tạo banner mới", description = "Tạo banner mới. Nếu không gửi active thì mặc định là true.")
    @PostMapping
    public ResponseEntity<BannerResponse> createBanner(@Valid @RequestBody BannerRequest request) throws Exception {
        BannerResponse bannerResponse = bannerService.createBanner(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(bannerResponse);
    }

    @Operation(summary = "Cập nhật banner", description = "Cập nhật thông tin banner theo id.")
    @PutMapping("/{id}")
    public ResponseEntity<BannerResponse> updateBanner(
            @PathVariable Long id,
            @Valid @RequestBody BannerRequest request) throws Exception {
        BannerResponse bannerResponse = bannerService.updateBanner(id, request);
        return ResponseEntity.ok(bannerResponse);
    }

    @Operation(summary = "Xóa banner", description = "Xóa mềm banner theo id: đặt active = false.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteBanner(@PathVariable Long id) throws Exception {
        bannerService.deleteBanner(id);
        return ResponseEntity.ok(new ApiResponse("Xóa thành công"));
    }

    @Operation(summary = "Lấy banner theo id", description = "Lấy chi tiết banner.")
    @GetMapping("/{id}")
    public ResponseEntity<BannerResponse> getBannerById(@PathVariable Long id) throws Exception {
        BannerResponse response = bannerService.getBannerById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lấy danh sách banner", description = "Lấy danh sách banner có phân trang.")
    @GetMapping
    public ResponseEntity<Page<BannerResponse>> getAllBanners(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        if (!SORTABLE_FIELDS.contains(sortBy)) {
            sortBy = "id";
        }

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return ResponseEntity.ok(bannerService.getAllBanners(pageable));
    }

    @Operation(summary = "Lấy danh sách banner đang hiển thị", description = "Lấy danh sách banner đang hoạt động, mới nhất trước. Dùng cho màn hình chủ của app.")
    @GetMapping("/active")
    public ResponseEntity<List<BannerResponse>> getActiveBanners() {
        return ResponseEntity.ok(bannerService.getActiveBanners());
    }

}
