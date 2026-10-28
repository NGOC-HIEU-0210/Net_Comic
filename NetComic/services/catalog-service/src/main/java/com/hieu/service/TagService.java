package com.hieu.service;

import com.hieu.payload.request.catalog.TagRequest;
import com.hieu.payload.response.catalog.TagResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TagService {

    TagResponse createTag(TagRequest request) throws Exception;
    TagResponse updateTag(Long id,TagRequest request) throws Exception;

    void deleteTag(Long id) throws Exception;

    TagResponse getTagById(Long id) throws Exception;

    Page<TagResponse> getAllTag(Pageable pageable);
    Page<TagResponse> searchTag(String keyword, Pageable pageable);
    Page<TagResponse> getTagByActiveTrue(Pageable pageable);

}
