package com.hieu.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hieu.enums.ComicStatus;
import com.hieu.mapper.ComicMapper;
import com.hieu.model.Author;
import com.hieu.model.Category;
import com.hieu.model.Comic;
import com.hieu.payload.request.catalog.ComicRequest;
import com.hieu.payload.response.catalog.ComicResponse;
import com.hieu.repository.AuthorRepository;
import com.hieu.repository.CategoryRepository;
import com.hieu.repository.ComicRepository;
import com.hieu.repository.ComicSpecification;
import com.hieu.service.ComicService;

@Service
public class ComicServiceImpl implements ComicService {

    private final ComicRepository comicRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;

    public ComicServiceImpl(ComicRepository comicRepository,
                            AuthorRepository authorRepository,
                            CategoryRepository categoryRepository) {
        this.comicRepository = comicRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ComicResponse createComic(ComicRequest request) throws Exception {
        String title = request.getTitle().trim();
        Author author = getActiveAuthor(request.getAuthorId());

        if (comicRepository.existsByTitleIgnoreCaseAndAuthorId(title, author.getId())) {
            throw new Exception("Tác giả này đã có truyện cùng tên");
        }

        Comic comic = ComicMapper.toEntity(request, author, resolveCategories(request.getCategoryIds(), Set.of()));

        Comic saved = comicRepository.save(comic);
        return ComicMapper.toResponse(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ComicResponse updateComic(Long id, ComicRequest request) throws Exception {
        Comic comic = findComic(id);
        String title = request.getTitle().trim();


        Author author = comic.getAuthor();
        if (!author.getId().equals(request.getAuthorId())) {
            author = getActiveAuthor(request.getAuthorId());
        }

        if (comicRepository.existsByTitleIgnoreCaseAndAuthorIdAndIdNot(title, author.getId(), id)) {
            throw new Exception("Tác giả này đã có truyện cùng tên");
        }

        // Thể loại đang gắn với truyện thì được giữ lại dù đã ngừng hoạt động
        Set<Long> currentCategoryIds = comic.getCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toSet());

        Set<Category> categories = resolveCategories(request.getCategoryIds(), currentCategoryIds);
        ComicMapper.updateEntity(comic, request, author, categories);

        Comic saved = comicRepository.save(comic);
        return ComicMapper.toResponse(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComic(Long id) throws Exception {
        Comic comic = findComic(id);
        comic.setStatus(ComicStatus.HIDDEN);
        comicRepository.save(comic);
    }

    @Override
    @Transactional(readOnly = true)
    public ComicResponse getComicById(Long id) throws Exception {
        return ComicMapper.toResponse(findComic(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComicResponse> searchComics(String keyword, ComicStatus status, Long authorId, Long categoryId,
                                            Pageable pageable) {
        return comicRepository
                .findAll(ComicSpecification.filter(keyword, status, authorId, categoryId), pageable)
                .map(ComicMapper::toResponse);
    }

    private Comic findComic(Long id) throws Exception {
        return comicRepository.findById(id).orElseThrow(
                () -> new Exception("Không tìm thấy truyện với id = " + id));
    }

    private Author getActiveAuthor(Long authorId) throws Exception {
        Author author = authorRepository.findById(authorId).orElseThrow(
                () -> new Exception("Không tìm thấy tác giả với id = " + authorId));
        if (!Boolean.TRUE.equals(author.getActive())) {
            throw new Exception("Tác giả \"" + author.getName() + "\" đã ngừng hoạt động");
        }
        return author;
    }

    private Set<Category> resolveCategories(Set<Long> ids, Set<Long> alreadyAssigned) throws Exception {
        List<Category> found = categoryRepository.findAllById(ids);

        Set<Long> foundIds = found.stream().map(Category::getId).collect(Collectors.toSet());
        List<Long> missing = ids.stream().filter(i -> !foundIds.contains(i)).sorted().toList();
        if (!missing.isEmpty()) {
            throw new Exception("Không tìm thấy thể loại với id = " + missing);
        }

        for (Category category : found) {
            boolean inactive = !Boolean.TRUE.equals(category.getActive());
            if (inactive && !alreadyAssigned.contains(category.getId())) {
                throw new Exception("Thể loại \"" + category.getName() + "\" đã ngừng hoạt động");
            }
        }
        return new HashSet<>(found);
    }
}
