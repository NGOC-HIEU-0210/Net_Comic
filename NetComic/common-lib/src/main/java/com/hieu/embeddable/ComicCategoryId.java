package com.hieu.embeddable;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;

@Embeddable
public class ComicCategoryId implements Serializable {

	private Long comicId;

	private Long categoryId;

	public ComicCategoryId() {
	}

	public ComicCategoryId(Long comicId, Long categoryId) {
		this.comicId = comicId;
		this.categoryId = categoryId;
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

	@Override
	public int hashCode() {
		return Objects.hash(comicId, categoryId);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ComicCategoryId)) {
			return false;
		}
		ComicCategoryId that = (ComicCategoryId) o;
		return Objects.equals(comicId, that.comicId) && Objects.equals(categoryId, that.categoryId);
	}
}
