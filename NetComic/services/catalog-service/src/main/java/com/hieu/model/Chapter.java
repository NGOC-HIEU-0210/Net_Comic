package com.hieu.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Nationalized;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.hieu.enums.ChapterStatus;

@Entity
@Table(name = "tbl_chapters",
        uniqueConstraints = @UniqueConstraint(name = "uk_chapter_comic_no",
                columnNames = {"comic_id", "chapter_no"}))   // 1 truyện không có 2 chương cùng số
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comic_id", nullable = false)
    private Comic comic;

    @Column(name = "chapter_no", nullable = false)
    private Integer chapterNo;


    @Column(name = "title", columnDefinition = "NVARCHAR(255)")
    private String title;

    // 0 = chương miễn phí
    @Column(name = "price_coins", nullable = false)
    private Integer priceCoins = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ChapterStatus status = ChapterStatus.DRAFT;

    // Hẹn giờ phát hành (BR10); null = không hẹn giờ
    @Column(name = "publish_at")
    private LocalDateTime publishAt;

    // Lịch sử duyệt: giữ lại, không xóa theo chương
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
