package com.hieu.repository;

import com.hieu.embeddable.ComicAssignmentId;
import com.hieu.model.ComicAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComicAssignmentRepository extends JpaRepository<ComicAssignment, ComicAssignmentId> {

    boolean existsById_ComicIdAndId_StaffId(Long comicId, Long staffId);


    @EntityGraph(attributePaths = "comic")
    @Override
    Page<ComicAssignment> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "comic")
    Page<ComicAssignment> findById_ComicId(Long comicId, Pageable pageable);

    @EntityGraph(attributePaths = "comic")
    Page<ComicAssignment> findById_StaffId(Long staffId, Pageable pageable);
}
