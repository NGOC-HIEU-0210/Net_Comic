package com.hieu.repository;

import com.hieu.enums.ReviewStatus;
import com.hieu.model.ReviewRequest;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRequestRepository extends JpaRepository<ReviewRequest, Long> {

    boolean existsByChapter_IdAndStatus(Long chapterId, ReviewStatus status);

    Optional<ReviewRequest> findByChapter_IdAndStatus(Long chapterId, ReviewStatus status);

    @EntityGraph(attributePaths = {"chapter", "chapter.comic"})
    @Override
    Page<ReviewRequest> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"chapter", "chapter.comic"})
    Page<ReviewRequest> findByStatus(ReviewStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"chapter", "chapter.comic"})
    Page<ReviewRequest> findByChapter_Id(Long chapterId, Pageable pageable);

    @EntityGraph(attributePaths = {"chapter", "chapter.comic"})
    Page<ReviewRequest> findBySubmittedBy(Long submittedBy, Pageable pageable);
}