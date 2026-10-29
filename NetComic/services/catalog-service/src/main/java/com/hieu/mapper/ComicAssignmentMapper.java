package com.hieu.mapper;

import com.hieu.model.ComicAssignment;
import com.hieu.payload.response.catalog.ComicAssignmentResponse;

public class ComicAssignmentMapper {

    public static ComicAssignmentResponse toResponse(ComicAssignment assignment) {
        if (assignment == null) {
            return null;
        }
        ComicAssignmentResponse response = new ComicAssignmentResponse();
        if (assignment.getComic() != null) {
            response.setComicId(assignment.getComic().getId());
            response.setComicTitle(assignment.getComic().getTitle());
        }
        response.setStaffId(assignment.getStaffId());
        return response;
    }
}
