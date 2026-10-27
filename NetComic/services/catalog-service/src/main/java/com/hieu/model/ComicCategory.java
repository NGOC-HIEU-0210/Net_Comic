package com.hieu.model;

import com.hieu.embeddable.ComicCategoryId;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_comic_categories")
@IdClass(ComicCategoryId.class)
public class ComicCategory {

    @Id
    private Long comicId;

    @Id
    private Long categoryId;

    public ComicCategory() {
    }

    public Long getComicId() {
        return comicId;
    }

    public void setComicId(Long comicId) {
        this.comicId = comicId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
