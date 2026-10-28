package com.hieu.repository;

import com.hieu.model.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    List<Author> findByActiveTrue();

    Page<Author> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    List<Author> findByNameContainingIgnoreCaseAndActiveTrue(String keyword);
}