package com.hieu.model;

import com.hieu.embeddable.ComicAssignmentId;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_comic_assignments")
@IdClass(ComicAssignmentId.class)
public class ComicAssignment {

	@Id
	private Long comicId;

	@Id
	private Long staffId;

	public ComicAssignment() {
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
