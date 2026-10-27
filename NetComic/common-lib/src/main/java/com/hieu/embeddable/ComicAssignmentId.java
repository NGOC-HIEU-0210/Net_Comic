package com.hieu.embeddable;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;

@Embeddable
public class ComicAssignmentId implements Serializable {

	private Long comicId;

	private Long staffId;

	public ComicAssignmentId() {
	}

	public ComicAssignmentId(Long comicId, Long staffId) {
		this.comicId = comicId;
		this.staffId = staffId;
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

	@Override
	public int hashCode() {
		return Objects.hash(comicId, staffId);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ComicAssignmentId)) {
			return false;
		}
		ComicAssignmentId that = (ComicAssignmentId) o;
		return Objects.equals(comicId, that.comicId) && Objects.equals(staffId, that.staffId);
	}
}
