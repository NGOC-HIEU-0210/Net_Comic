package com.hieu.model;
import jakarta.persistence.*;
@Entity
@Table(name = "tbl_chapter_pages",
        uniqueConstraints = @UniqueConstraint(name = "uk_page_chapter_no",
                columnNames = { "chapter_id", "page_no" }))
public class ChapterPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chapter_id", nullable = false)    // tham chiếu Catalog, không có FK
    private Long chapterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_job_id")
    private ImportJob importJob;

    @Column(name = "page_no", nullable = false)
    private Integer pageNo;

    @Column(name = "image_key", nullable = false, length = 512)
    private String imageKey;

    public ChapterPage() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChapterId() {
        return chapterId;
    }

    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }

    public ImportJob getImportJob() {
        return importJob;
    }

    public void setImportJob(ImportJob importJob) {
        this.importJob = importJob;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public String getImageKey() {
        return imageKey;
    }

    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }
}