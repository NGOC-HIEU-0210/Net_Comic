
package com.hieu.mapper;

import com.hieu.model.Tag;
import com.hieu.payload.request.catalog.TagRequest;
import com.hieu.payload.response.catalog.TagResponse;


public class TagMapper {

    public static Tag toEntity(TagRequest request) {
        if (request == null) {
            return null;
        }

        Tag tag = new Tag();
        tag.setName(request.getName());
        tag.setActive(request.getActive());

        return tag;
    }

    public static TagResponse toResponse(Tag tag) {
        if (tag == null) {
            return null;
        }

        TagResponse response = new TagResponse();
        response.setId(tag.getId());
        response.setName(tag.getName());
        response.setActive(tag.getActive());

        return response;
    }

    public static Tag updateEntity(Tag tag, TagRequest request) {
        if (tag == null || request == null) {
            return tag;
        }

        tag.setName(request.getName());
        tag.setActive(request.getActive());

        return tag;
    }
}
