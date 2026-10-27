package com.hieu.embeddable;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;

@Embeddable
public class ComicAuthorId implements Serializable {

	private Long comicId;

	private Long authorId;

	public ComicAuthorId() {
	}

	public ComicAuthorId(Long comicId, Long authorId) {
		this.comicId = comicId;
		this.authorId = authorId;
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

	@Override
	public int hashCode() {
		return Objects.hash(comicId, authorId);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ComicAuthorId)) {
			return false;
		}
		ComicAuthorId that = (ComicAuthorId) o;
		return Objects.equals(comicId, that.comicId) && Objects.equals(authorId, that.authorId);
	}
}
