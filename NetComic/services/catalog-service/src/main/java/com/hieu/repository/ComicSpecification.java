package com.hieu.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.hieu.enums.ComicStatus;
import com.hieu.model.Category;
import com.hieu.model.Comic;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public final class ComicSpecification {

    private ComicSpecification() {
    }


    public static Specification<Comic> filter(String keyword, ComicStatus status, Long authorId, Long categoryId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + escapeLike(keyword.trim().toLowerCase()) + "%";
                predicates.add(cb.like(cb.lower(root.<String>get("title")), pattern, '\\'));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (authorId != null) {
                predicates.add(cb.equal(root.get("author").get("id"), authorId));
            }
            if (categoryId != null) {
                Join<Comic, Category> categories = root.join("categories", JoinType.INNER);
                predicates.add(cb.equal(categories.get("id"), categoryId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Người dùng gõ % _ [ thì coi như ký tự thường, không phải ký tự đại diện
    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
                .replace("[", "\\[");
    }
}
