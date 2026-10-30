package com.hieu.mapper;

import com.hieu.enums.ReviewStatus;
import com.hieu.model.Chapter;
import com.hieu.model.ReviewRequest;
import com.hieu.payload.request.catalog.ReviewSubmitRequest;
import com.hieu.payload.response.catalog.ReviewRequestResponse;

public class ReviewRequestMapper {

    public static ReviewRequest toEntity(ReviewSubmitRequest request, Chapter chapter) {
        if (request == null) {
            return null;
        }
        ReviewRequest review = new ReviewRequest();
        review.setChapter(chapter);
        review.setSubmittedBy(request.getStaffId());
        review.setStatus(ReviewStatus.PENDING);
        return review;
    }

    public static ReviewRequestResponse toResponse(ReviewRequest review) {
        if (review == null) {
            return null;
        }

        ReviewRequestResponse response = new ReviewRequestResponse();
        response.setId(review.getId());
        response.setSubmittedBy(review.getSubmittedBy());
        response.setReviewedBy(review.getReviewedBy());
        response.setStatus(review.getStatus());
        response.setReason(review.getReason());

        Chapter chapter = review.getChapter();
        if (chapter != null) {
            response.setChapterId(chapter.getId());
            response.setChapterNo(chapter.getChapterNo());
            response.setChapterTitle(chapter.getTitle());
            response.setChapterStatus(chapter.getStatus());
            if (chapter.getComic() != null) {
                response.setComicId(chapter.getComic().getId());
                response.setComicTitle(chapter.getComic().getTitle());
            }
        }
        return response;
    }
}
