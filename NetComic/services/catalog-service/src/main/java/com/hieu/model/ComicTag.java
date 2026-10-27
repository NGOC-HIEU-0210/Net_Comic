package com.hieu.model;

import com.hieu.embeddable.ComicTagId;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_comic_tags")
@IdClass(ComicTagId.class)
public class ComicTag {

    @Id
    private Long comicId;

    @Id
    private Long tagId;

    public ComicTag() {
    }

    public Long getComicId() {
        return comicId;
    }

    public void setComicId(Long comicId) {
        this.comicId = comicId;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }
}
