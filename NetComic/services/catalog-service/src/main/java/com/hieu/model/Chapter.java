package com.hieu.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.hieu.enums.ChapterStatus;

@Entity
@Table(name = "tbl_chapters")
public class Chapter {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "comic_id")
	private Comic comic;

	@Column(name = "chapter_no")
	private Integer chapterNo;

	@Column(name = "title", length = 255)
	private String title;

	@Column(name = "price_coins")
	private Integer priceCoins;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private ChapterStatus status;

	@Column(name = "publish_at")
	private LocalDateTime publishAt;

	@OneToMany(mappedBy = "chapter")
	private List<ReviewRequest> reviewRequests = new ArrayList<>();

	public Chapter() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Comic getComic() {
		return comic;
	}

	public void setComic(Comic comic) {
		this.comic = comic;
	}

	public Integer getChapterNo() {
		return chapterNo;
	}

	public void setChapterNo(Integer chapterNo) {
		this.chapterNo = chapterNo;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getPriceCoins() {
		return priceCoins;
	}

	public void setPriceCoins(Integer priceCoins) {
		this.priceCoins = priceCoins;
	}

	public ChapterStatus getStatus() {
		return status;
	}

	public void setStatus(ChapterStatus status) {
		this.status = status;
	}

	public LocalDateTime getPublishAt() {
		return publishAt;
	}

	public void setPublishAt(LocalDateTime publishAt) {
		this.publishAt = publishAt;
	}

	public List<ReviewRequest> getReviewRequests() {
		return reviewRequests;
	}

	public void setReviewRequests(List<ReviewRequest> reviewRequests) {
		this.reviewRequests = reviewRequests;
	}
}
