package com.hieu.payload.response.catalog;

import com.hieu.enums.ChapterStatus;

import java.time.LocalDateTime;

public class ChapterResponse {

    private Long id;
    private Long comicId;
    private String comicTitle;
    private Integer chapterNo;
    private String title;
    private Integer priceCoins;
    private ChapterStatus status;
    private LocalDateTime publishAt;

    public ChapterResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getChapterNo() {
        return chapterNo;
    }

    public void setChapterNo(Integer chapterNo) {
        this.chapterNo = chapterNo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPriceCoins() {
        return priceCoins;
    }

    public void setPriceCoins(Integer priceCoins) {
        this.priceCoins = priceCoins;
    }

    public ChapterStatus getStatus() {
        return status;
    }

    public void setStatus(ChapterStatus status) {
        this.status = status;
    }

    public LocalDateTime getPublishAt() {
        return publishAt;
    }

    public void setPublishAt(LocalDateTime publishAt) {
        this.publishAt = publishAt;
    }
}