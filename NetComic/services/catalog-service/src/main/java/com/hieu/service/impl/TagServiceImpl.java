package com.hieu.service.impl;

import com.hieu.mapper.TagMapper;
import com.hieu.model.Tag;
import com.hieu.payload.request.catalog.TagRequest;
import com.hieu.payload.response.catalog.TagResponse;
import com.hieu.repository.TagRepository;
import com.hieu.service.TagService;
import org.eclipse.jetty.io.QuietException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;

    public TagServiceImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    @Transactional
    public TagResponse createTag(TagRequest request) throws Exception {
        if (tagRepository.existsByName(request.getName())) {
            throw new Exception("Tên Tag đã tồn tại");
        }
        Tag tag = TagMapper.toEntity(request);
        Tag saved = tagRepository.save(tag);
        return TagMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TagResponse updateTag(Long id, TagRequest request) throws Exception {
       Tag tag = tagRepository.findById(id).orElseThrow(
               () -> new Exception("Không tìm thấy Tag có id = " + id)
       );
       if (tagRepository.existsByNameAndIdNot(tag.getName(), id)) {
           throw new Exception("Tên Tag đã tồn tại");
       }
       TagMapper.updateEntity(tag, request);
       Tag saved = tagRepository.save(tag);
        return TagMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteTag(Long id) throws Exception {
        Tag tag = tagRepository.findById(id).orElseThrow(
                () -> new Exception("Không tìm thấy Tag có id = " + id)
        );

        tag.setActive(Boolean.FALSE);
        tagRepository.save(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public TagResponse getTagById(Long id) throws Exception {
        Tag tag = tagRepository.findById(id).orElseThrow(
                () -> new Exception("Không tìm thấy Tag có id = " + id)
        );
        return TagMapper.toResponse(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TagResponse> getAllTag(Pageable pageable) {
        return tagRepository.findAll(pageable).map(TagMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TagResponse> searchTag(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()){
            return getAllTag(pageable);
        }

        return tagRepository.findByNameContainingIgnoreCase(keyword, pageable).map(TagMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TagResponse> getTagByActiveTrue(Pageable pageable) {
        return tagRepository.findByActiveTrue(pageable).map(TagMapper::toResponse);
    }
}
