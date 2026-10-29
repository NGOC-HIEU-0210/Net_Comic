package com.hieu.payload.request.catalog;

import jakarta.validation.constraints.NotNull;

public class ComicAssignmentRequest {

    @NotNull(message = "comicId không được để trống")
    private Long comicId;

    @NotNull(message = "staffId không được để trống")
    private Long staffId;

    public ComicAssignmentRequest() {
    }

    public Long getComicId() {
        return comicId;
    }

    public void setComicId(Long comicId) {
        this.comicId = comicId;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }
}
