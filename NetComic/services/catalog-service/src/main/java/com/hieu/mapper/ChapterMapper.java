package com.hieu.mapper;

import com.hieu.model.Chapter;
import com.hieu.model.Comic;
import com.hieu.payload.request.catalog.ChapterRequest;
import com.hieu.payload.response.catalog.ChapterResponse;

public class ChapterMapper {

    public static Chapter toEntity(ChapterRequest request, Comic comic) {
        Chapter chapter = new Chapter();
        chapter.setComic(comic);
        chapter.setChapterNo(request.getChapterNo());
        chapter.setTitle(request.getTitle() != null ? request.getTitle().trim() : null);
        chapter.setPriceCoins(request.getPriceCoins() != null ? request.getPriceCoins() : 0);
        chapter.setStatus(request.getStatus());
        chapter.setPublishAt(request.getPublishAt());
        return chapter;
    }

    public static void updateEntity(Chapter chapter, ChapterRequest request, Comic comic) {
        if (comic != null) {
            chapter.setComic(comic);
        }
        chapter.setChapterNo(request.getChapterNo());
        chapter.setTitle(request.getTitle() != null ? request.getTitle().trim() : null);
        if (request.getPriceCoins() != null) {
            chapter.setPriceCoins(request.getPriceCoins());
        }
        chapter.setStatus(request.getStatus());
        chapter.setPublishAt(request.getPublishAt());
    }

    public static ChapterResponse toResponse(Chapter chapter) {
        ChapterResponse response = new ChapterResponse();
        response.setId(chapter.getId());
        if (chapter.getComic() != null) {
            response.setComicId(chapter.getComic().getId());
            response.setComicTitle(chapter.getComic().getTitle());
        }
        response.setChapterNo(chapter.getChapterNo());
        response.setTitle(chapter.getTitle());
        response.setPriceCoins(chapter.getPriceCoins());
        response.setStatus(chapter.getStatus());
        response.setPublishAt(chapter.getPublishAt());
        return response;
    }
}