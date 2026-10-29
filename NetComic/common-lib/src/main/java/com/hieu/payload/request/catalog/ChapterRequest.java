package com.hieu.payload.request.catalog;

import com.hieu.enums.ChapterStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class ChapterRequest {

    @NotNull(message = "ID truyện không được để trống")
    private Long comicId;

    @NotNull(message = "Số chương không được để trống")
    @Min(value = 0, message = "Số chương phải lớn hơn hoặc bằng 0")
    private Integer chapterNo;

    @Size(max = 255, message = "Tiêu đề chương không được vượt quá 255 ký tự")
    private String title;

    @NotNull(message = "Giá chương (xu) không được để trống")
    @Min(value = 0, message = "Giá chương không được âm (0 là miễn phí)")
    private Integer priceCoins = 0;

    @NotNull(message = "Trạng thái chương không được để trống")
    private ChapterStatus status = ChapterStatus.DRAFT;

    // Null nếu phát hành ngay hoặc lưu nháp không hẹn giờ
    private LocalDateTime publishAt;

    public ChapterRequest() {
    }

    public Long getComicId() {
        return comicId;
    }

    public void setComicId(Long comicId) {
        this.comicId = comicId;
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