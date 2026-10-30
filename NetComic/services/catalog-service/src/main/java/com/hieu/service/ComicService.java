package com.hieu.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hieu.enums.ComicStatus;
import com.hieu.payload.request.catalog.ComicRequest;
import com.hieu.payload.response.catalog.ComicResponse;

public interface ComicService {

    ComicResponse createComic(ComicRequest request) throws Exception;

    ComicResponse updateComic(Long id, ComicRequest request) throws Exception;

    // Xóa mềm: chuyển truyện sang trạng thái HIDDEN
    void deleteComic(Long id) throws Exception;

    ComicResponse getComicById(Long id) throws Exception;

    Page<ComicResponse> searchComics(String keyword, ComicStatus status, Long authorId, Long categoryId,
                                     Pageable pageable);
}
