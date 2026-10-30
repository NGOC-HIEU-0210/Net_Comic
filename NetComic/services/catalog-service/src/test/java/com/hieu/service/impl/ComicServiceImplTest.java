package com.hieu.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.hieu.enums.ComicStatus;
import com.hieu.model.Author;
import com.hieu.model.Category;
import com.hieu.model.Comic;
import com.hieu.payload.request.catalog.ComicRequest;
import com.hieu.payload.response.catalog.ComicResponse;
import com.hieu.repository.AuthorRepository;
import com.hieu.repository.CategoryRepository;
import com.hieu.repository.ComicRepository;

@ExtendWith(MockitoExtension.class)
class ComicServiceImplTest {

    @Mock
    private ComicRepository comicRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ComicServiceImpl comicService;

    private Author author;
    private Category action;
    private Category romance;

    @BeforeEach
    void setUp() {
        author = author(1L, "Oda", true);
        action = category(10L, "Hành động", true);
        romance = category(11L, "Tình cảm", true);
    }

    // ---------- create ----------

    @Test
    void createComic_success_defaultsToDraft() throws Exception {
        ComicRequest request = request("  One Piece  ", 1L, Set.of(10L, 11L));

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(comicRepository.existsByTitleIgnoreCaseAndAuthorId("One Piece", 1L)).thenReturn(false);
        when(categoryRepository.findAllById(request.getCategoryIds())).thenReturn(List.of(action, romance));
        when(comicRepository.save(any(Comic.class))).thenAnswer(inv -> {
            Comic c = inv.getArgument(0);
            c.setId(100L);
            return c;
        });

        ComicResponse response = comicService.createComic(request);

        assertEquals(100L, response.getId());
        assertEquals("One Piece", response.getTitle());
        assertEquals(ComicStatus.DRAFT, response.getStatus());
        assertEquals(1L, response.getAuthorId());
        assertEquals("Oda", response.getAuthorName());
        assertEquals(2, response.getCategories().size());
        assertEquals("Hành động", response.getCategories().get(0).getName());
    }

    @Test
    void createComic_usesStatusFromRequest() throws Exception {
        ComicRequest request = request("Naruto", 1L, Set.of(10L));
        request.setStatus(ComicStatus.PUBLISHED);

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(categoryRepository.findAllById(request.getCategoryIds())).thenReturn(List.of(action));
        when(comicRepository.save(any(Comic.class))).thenAnswer(inv -> inv.getArgument(0));

        ComicResponse response = comicService.createComic(request);

        assertEquals(ComicStatus.PUBLISHED, response.getStatus());
    }

    @Test
    void createComic_duplicateTitleForSameAuthor_throws() {
        ComicRequest request = request("One Piece", 1L, Set.of(10L));

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(comicRepository.existsByTitleIgnoreCaseAndAuthorId("One Piece", 1L)).thenReturn(true);

        Exception ex = assertThrows(Exception.class, () -> comicService.createComic(request));

        assertEquals("Tác giả này đã có truyện cùng tên", ex.getMessage());
        verify(comicRepository, never()).save(any());
    }

    @Test
    void createComic_authorNotFound_throws() {
        ComicRequest request = request("One Piece", 99L, Set.of(10L));
        when(authorRepository.findById(99L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(Exception.class, () -> comicService.createComic(request));

        assertEquals("Không tìm thấy tác giả với id = 99", ex.getMessage());
        verify(comicRepository, never()).save(any());
    }

    @Test
    void createComic_authorInactive_throws() {
        ComicRequest request = request("One Piece", 1L, Set.of(10L));
        author.setActive(false);
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));

        Exception ex = assertThrows(Exception.class, () -> comicService.createComic(request));

        assertTrue(ex.getMessage().contains("đã ngừng hoạt động"));
        verify(comicRepository, never()).save(any());
    }

    @Test
    void createComic_categoryNotFound_throws() {
        ComicRequest request = request("One Piece", 1L, Set.of(10L, 77L));

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(categoryRepository.findAllById(request.getCategoryIds())).thenReturn(List.of(action));

        Exception ex = assertThrows(Exception.class, () -> comicService.createComic(request));

        assertEquals("Không tìm thấy thể loại với id = [77]", ex.getMessage());
        verify(comicRepository, never()).save(any());
    }

    @Test
    void createComic_categoryInactive_throws() {
        ComicRequest request = request("One Piece", 1L, Set.of(10L));
        action.setActive(false);

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(categoryRepository.findAllById(request.getCategoryIds())).thenReturn(List.of(action));

        Exception ex = assertThrows(Exception.class, () -> comicService.createComic(request));

        assertTrue(ex.getMessage().contains("Hành động"));
        verify(comicRepository, never()).save(any());
    }

    // ---------- update ----------

    @Test
    void updateComic_success_replacesCategoriesAndKeepsStatusWhenNull() throws Exception {
        Comic comic = comic(100L, "Old title", author, ComicStatus.PUBLISHED, action);
        ComicRequest request = request("New title", 1L, Set.of(11L));

        when(comicRepository.findById(100L)).thenReturn(Optional.of(comic));
        when(comicRepository.existsByTitleIgnoreCaseAndAuthorIdAndIdNot("New title", 1L, 100L)).thenReturn(false);
        when(categoryRepository.findAllById(request.getCategoryIds())).thenReturn(List.of(romance));
        when(comicRepository.save(any(Comic.class))).thenAnswer(inv -> inv.getArgument(0));

        ComicResponse response = comicService.updateComic(100L, request);

        assertEquals("New title", response.getTitle());
        assertEquals(ComicStatus.PUBLISHED, response.getStatus());
        assertEquals(1, response.getCategories().size());
        assertEquals("Tình cảm", response.getCategories().get(0).getName());
        // tác giả không đổi nên không phải tra lại
        verify(authorRepository, never()).findById(anyLong());
    }

    @Test
    void updateComic_changeAuthor_requiresActiveAuthor() {
        Comic comic = comic(100L, "Title", author, ComicStatus.DRAFT, action);
        Author other = author(2L, "Kishimoto", false);
        ComicRequest request = request("Title", 2L, Set.of(10L));

        when(comicRepository.findById(100L)).thenReturn(Optional.of(comic));
        when(authorRepository.findById(2L)).thenReturn(Optional.of(other));

        Exception ex = assertThrows(Exception.class, () -> comicService.updateComic(100L, request));

        assertTrue(ex.getMessage().contains("Kishimoto"));
        verify(comicRepository, never()).save(any());
    }

    @Test
    void updateComic_keepsInactiveCategoryThatIsAlreadyAssigned() throws Exception {
        action.setActive(false);
        Comic comic = comic(100L, "Title", author, ComicStatus.DRAFT, action);
        ComicRequest request = request("Title", 1L, Set.of(10L));

        when(comicRepository.findById(100L)).thenReturn(Optional.of(comic));
        when(categoryRepository.findAllById(request.getCategoryIds())).thenReturn(List.of(action));
        when(comicRepository.save(any(Comic.class))).thenAnswer(inv -> inv.getArgument(0));

        ComicResponse response = comicService.updateComic(100L, request);

        assertEquals(1, response.getCategories().size());
    }

    @Test
    void updateComic_duplicateTitle_throws() {
        Comic comic = comic(100L, "Old", author, ComicStatus.DRAFT, action);
        ComicRequest request = request("Other", 1L, Set.of(10L));

        when(comicRepository.findById(100L)).thenReturn(Optional.of(comic));
        when(comicRepository.existsByTitleIgnoreCaseAndAuthorIdAndIdNot("Other", 1L, 100L)).thenReturn(true);

        Exception ex = assertThrows(Exception.class, () -> comicService.updateComic(100L, request));

        assertEquals("Tác giả này đã có truyện cùng tên", ex.getMessage());
        verify(comicRepository, never()).save(any());
    }

    @Test
    void updateComic_notFound_throws() {
        when(comicRepository.findById(5L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(Exception.class,
                () -> comicService.updateComic(5L, request("A", 1L, Set.of(10L))));

        assertEquals("Không tìm thấy truyện với id = 5", ex.getMessage());
    }

    // ---------- delete / get / search ----------

    @Test
    void deleteComic_setsStatusHidden() throws Exception {
        Comic comic = comic(100L, "Title", author, ComicStatus.PUBLISHED, action);
        when(comicRepository.findById(100L)).thenReturn(Optional.of(comic));

        comicService.deleteComic(100L);

        assertEquals(ComicStatus.HIDDEN, comic.getStatus());
        verify(comicRepository).save(comic);
        verify(comicRepository, never()).delete(any(Comic.class));
        verify(comicRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteComic_notFound_throws() {
        when(comicRepository.findById(5L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(Exception.class, () -> comicService.deleteComic(5L));

        assertEquals("Không tìm thấy truyện với id = 5", ex.getMessage());
    }

    @Test
    void getComicById_success() throws Exception {
        Comic comic = comic(100L, "Title", author, ComicStatus.DRAFT, action);
        when(comicRepository.findById(100L)).thenReturn(Optional.of(comic));

        ComicResponse response = comicService.getComicById(100L);

        assertEquals(100L, response.getId());
        assertEquals("Oda", response.getAuthorName());
    }

    @Test
    void getComicById_notFound_throws() {
        when(comicRepository.findById(5L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(Exception.class, () -> comicService.getComicById(5L));

        assertEquals("Không tìm thấy truyện với id = 5", ex.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchComics_mapsPageFromRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Comic comic = comic(100L, "Title", author, ComicStatus.PUBLISHED, action);
        when(comicRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(comic), pageable, 1));

        Page<ComicResponse> result = comicService.searchComics("tit", ComicStatus.PUBLISHED, 1L, 10L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Title", result.getContent().get(0).getTitle());
    }

    // ---------- helpers ----------

    private static ComicRequest request(String title, Long authorId, Set<Long> categoryIds) {
        ComicRequest request = new ComicRequest();
        request.setTitle(title);
        request.setAuthorId(authorId);
        request.setCategoryIds(categoryIds);
        return request;
    }

    private static Author author(Long id, String name, boolean active) {
        Author a = new Author();
        a.setId(id);
        a.setName(name);
        a.setActive(active);
        a.setBio("bio");
        return a;
    }

    private static Category category(Long id, String name, boolean active) {
        Category c = new Category();
        c.setId(id);
        c.setName(name);
        c.setActive(active);
        return c;
    }

    private static Comic comic(Long id, String title, Author author, ComicStatus status, Category... categories) {
        Comic c = new Comic();
        c.setId(id);
        c.setTitle(title);
        c.setAuthor(author);
        c.setStatus(status);
        c.setCategories(new HashSet<>(List.of(categories)));
        return c;
    }
}
