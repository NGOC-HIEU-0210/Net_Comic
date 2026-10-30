package com.hieu.payload.response.catalog;

import com.hieu.enums.ChapterStatus;
import com.hieu.enums.ReviewStatus;

public class ReviewRequestResponse {

    private Long id;
    private Long chapterId;
    private Integer chapterNo;
    private String chapterTitle;
    private ChapterStatus chapterStatus;
    private Long comicId;
    private String comicTitle;
    private Long submittedBy;
    private Long reviewedBy;
    private ReviewStatus status;
    private String reason;

    public ReviewRequestResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChapterId() {
        return chapterId;
    }

    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }

    public Integer getChapterNo() {
        return chapterNo;
    }

    public void setChapterNo(Integer chapterNo) {
        this.chapterNo = chapterNo;
    }

    public String getChapterTitle() {
        return chapterTitle;
    }

    public void setChapterTitle(String chapterTitle) {
        this.chapterTitle = chapterTitle;
    }

    public ChapterStatus getChapterStatus() {
        return chapterStatus;
    }

    public void setChapterStatus(ChapterStatus chapterStatus) {
        this.chapterStatus = chapterStatus;
    }

    public Long getComicId() {
        return comicId;
    }

    public void setComicId(Long comicId) {
        this.comicId = comicId;
    }

    public String getComicTitle() {
        return comicTitle;
    }

    public void setComicTitle(String comicTitle) {
        this.comicTitle = comicTitle;
    }

    public Long getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(Long submittedBy) {
        this.submittedBy = submittedBy;
    }

    public Long getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(Long reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}