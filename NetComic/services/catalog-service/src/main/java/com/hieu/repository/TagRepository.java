package com.hieu.repository;

import com.hieu.model.Category;
import com.hieu.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    Page<Tag> findByActiveTrue(Pageable pageable);

    Page<Tag> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
