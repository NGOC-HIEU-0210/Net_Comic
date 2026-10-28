package com.hieu.model;

import com.hieu.embeddable.ComicAssignmentId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;


@Entity
@Table(name = "tbl_comic_assignments")
public class ComicAssignment {

    @EmbeddedId
    private ComicAssignmentId id = new ComicAssignmentId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("comicId")
    @JoinColumn(name = "comic_id")
    private Comic comic;

    public ComicAssignment() {
    }

    public ComicAssignment(Comic comic, Long staffId) {
        this.comic = comic;
        this.id = new ComicAssignmentId(comic.getId(), staffId);
    }

    public ComicAssignmentId getId() {
        return id;
    }

    public Comic getComic() {
        return comic;
    }

    public void setComic(Comic comic) {
        this.comic = comic;
    }

    public Long getStaffId() {
        return id.getStaffId();
    }

    public void setStaffId(Long staffId) {
        this.id.setStaffId(staffId);
    }
}
