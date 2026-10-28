package com.hieu.repository;

import com.hieu.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<Category> findByActiveTrue();

    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Category> findByNameContainingIgnoreCaseAndActiveTrue(String keyword);

}