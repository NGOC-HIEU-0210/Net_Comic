package com.hieu.service;

import com.hieu.enums.ReviewStatus;
import com.hieu.payload.request.catalog.ReviewApproveRequest;
import com.hieu.payload.request.catalog.ReviewRejectRequest;
import com.hieu.payload.request.catalog.ReviewSubmitRequest;
import com.hieu.payload.response.catalog.ReviewRequestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewRequestService {

    // Staff gửi duyệt: tạo phiếu PENDING, chương DRAFT -> PENDING_REVIEW
    ReviewRequestResponse submit(ReviewSubmitRequest request) throws Exception;

    ReviewRequestResponse getById(Long id) throws Exception;

    // status = null thì lấy tất cả
    Page<ReviewRequestResponse> getAll(ReviewStatus status, Pageable pageable);

    Page<ReviewRequestResponse> getByChapter(Long chapterId, Pageable pageable) throws Exception;

    Page<ReviewRequestResponse> getByStaff(Long staffId, Pageable pageable);

    Page<ReviewRequestResponse> getPending(Pageable pageable);

    // Admin duyệt. Chương -> SCHEDULED nếu publishAt còn ở tương lai, ngược lại PUBLISHED
    ReviewRequestResponse approve(Long id, ReviewApproveRequest request) throws Exception;

    // Admin từ chối, bắt buộc lý do. Chương -> DRAFT để staff sửa rồi gửi lại
    ReviewRequestResponse reject(Long id, ReviewRejectRequest request) throws Exception;

    // Staff hủy phiếu của chính mình khi còn PENDING. Không xóa phiếu đã xử lý
    void cancel(Long id, Long staffId) throws Exception;
}