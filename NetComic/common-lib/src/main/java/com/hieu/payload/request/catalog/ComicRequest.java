package com.hieu.payload.request.catalog;

import java.util.Set;

import com.hieu.enums.ComicStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ComicRequest {

    @NotBlank(message = "Tên truyện không được để trống")
    @Size(max = 255, message = "Tên truyện tối đa 255 ký tự")
    private String title;

    @Size(max = 5000, message = "Mô tả tối đa 5000 ký tự")
    private String description;

    @Size(max = 512, message = "Đường dẫn ảnh bìa tối đa 512 ký tự")
    private String coverUrl;

    @NotNull(message = "Phải chọn tác giả")
    private Long authorId;

    @NotEmpty(message = "Phải chọn ít nhất một thể loại")
    private Set<@NotNull(message = "Id thể loại không được null") Long> categoryIds;


    private ComicStatus status;

    public ComicRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Set<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(Set<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public ComicStatus getStatus() {
        return status;
    }

    public void setStatus(ComicStatus status) {
        this.status = status;
    }
}
