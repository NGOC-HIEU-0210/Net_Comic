package com.hieu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.hieu.model.Comic;

@Repository
public interface ComicRepository extends JpaRepository<Comic, Long>, JpaSpecificationExecutor<Comic> {

    // Cùng một tác giả không có hai truyện trùng tên
    boolean existsByTitleIgnoreCaseAndAuthorId(String title, Long authorId);

    boolean existsByTitleIgnoreCaseAndAuthorIdAndIdNot(String title, Long authorId, Long id);
}
