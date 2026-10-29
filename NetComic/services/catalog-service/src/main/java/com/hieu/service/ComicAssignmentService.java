package com.hieu.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hieu.payload.request.catalog.ComicAssignmentRequest;
import com.hieu.payload.request.catalog.ReassignStaffRequest;
import com.hieu.payload.response.catalog.ComicAssignmentResponse;

public interface ComicAssignmentService {

    // Phân công nhân viên cho truyện
    ComicAssignmentResponse createComicAssignment(ComicAssignmentRequest request) throws Exception;

    // Chuyển truyện từ nhân viên staffId sang nhân viên mới
    ComicAssignmentResponse updateComicAssignment(Long comicId, Long staffId, ReassignStaffRequest request)
            throws Exception;

    // Gỡ phân công (xóa hẳn dòng, bảng không có cột trạng thái)
    void deleteComicAssignment(Long comicId, Long staffId) throws Exception;

    ComicAssignmentResponse getComicAssignment(Long comicId, Long staffId) throws Exception;

    Page<ComicAssignmentResponse> getAllComicAssignments(Pageable pageable);

    // Các nhân viên được giao một truyện
    Page<ComicAssignmentResponse> getAssignmentsByComic(Long comicId, Pageable pageable) throws Exception;

    // Các truyện được giao cho một nhân viên
    Page<ComicAssignmentResponse> getAssignmentsByStaff(Long staffId, Pageable pageable);

    //  nhân viên có được sửa truyện này không (Admin bỏ qua bước này ở nơi gọi)
    boolean isStaffAssigned(Long comicId, Long staffId);
}
