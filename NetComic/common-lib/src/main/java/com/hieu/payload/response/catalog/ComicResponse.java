package com.hieu.payload.response.catalog;

import java.util.ArrayList;
import java.util.List;

import com.hieu.enums.ComicStatus;

public class ComicResponse {

    private Long id;
    private String title;
    private String description;
    private String coverUrl;
    private ComicStatus status;

    // Chỉ trả id và tên tác giả, không trả cả AuthorResponse để tránh lồng nhau
    private Long authorId;
    private String authorName;

    private List<CategoryResponse> categories = new ArrayList<>();

    public ComicResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public ComicStatus getStatus() {
        return status;
    }

    public void setStatus(ComicStatus status) {
        this.status = status;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public List<CategoryResponse> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryResponse> categories) {
        this.categories = categories;
    }
}
