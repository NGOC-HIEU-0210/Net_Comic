package com.hieu.mapper;

import com.hieu.model.Comic;
import com.hieu.model.ComicAssignment;
import com.hieu.payload.request.catalog.ComicAssignmentRequest;
import com.hieu.payload.response.catalog.ComicAssignmentResponse;

public class ComicAssignmentMapper {

    public static ComicAssignment toEntity(ComicAssignmentRequest request, Comic comic) {
        if (request == null) {
            return null;
        }
        return new ComicAssignment(comic, request.getStaffId());
    }

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
