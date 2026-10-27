package com.hieu.model;

import com.hieu.embeddable.ComicAuthorId;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_comic_authors")
@IdClass(ComicAuthorId.class)
public class ComicAuthor {

    @Id
    private Long comicId;

    @Id
    private Long authorId;

    public ComicAuthor() {
    }

    public Long getComicId() {
        return comicId;
    }

    public void setComicId(Long comicId) {
        this.comicId = comicId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }
}
