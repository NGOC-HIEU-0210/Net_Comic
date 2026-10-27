package com.hieu.embeddable;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;

@Embeddable
public class ComicTagId implements Serializable {

	private Long comicId;

	private Long tagId;

	public ComicTagId() {
	}

	public ComicTagId(Long comicId, Long tagId) {
		this.comicId = comicId;
		this.tagId = tagId;
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

	@Override
	public int hashCode() {
		return Objects.hash(comicId, tagId);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ComicTagId)) {
			return false;
		}
		ComicTagId that = (ComicTagId) o;
		return Objects.equals(comicId, that.comicId) && Objects.equals(tagId, that.tagId);
	}
}
