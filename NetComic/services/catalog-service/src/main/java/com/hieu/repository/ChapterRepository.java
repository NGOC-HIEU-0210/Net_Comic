package com.hieu.repository;

import com.hieu.enums.ChapterStatus;
import com.hieu.model.Chapter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    boolean existsByComicIdAndChapterNo(Long comicId, Integer chapterNo);

    boolean existsByComicIdAndChapterNoAndIdNot(Long comicId, Integer chapterNo, Long id);

    Page<Chapter> findByComicId(Long comicId, Pageable pageable);


    Page<Chapter> findByComicIdAndStatus(Long comicId, ChapterStatus status, Pageable pageable);


    List<Chapter> findByStatusAndPublishAtLessThanEqual(ChapterStatus status, LocalDateTime now);
}