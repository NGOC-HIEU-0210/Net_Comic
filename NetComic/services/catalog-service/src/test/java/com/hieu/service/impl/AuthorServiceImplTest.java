package com.hieu.service.impl;

import com.hieu.model.Author;
import com.hieu.payload.request.catalog.AuthorRequest;
import com.hieu.payload.response.catalog.AuthorResponse;
import com.hieu.repository.AuthorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorServiceImplTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorServiceImpl authorService;

    @Test
    void createAuthor_whenNameIsNew_savesNameActiveAndBio() throws Exception {
        AuthorRequest request = request("Nguyễn Nhật Ánh", true, "Nhà văn chuyên viết cho tuổi mới lớn");
        when(authorRepository.existsByNameIgnoreCase("Nguyễn Nhật Ánh")).thenReturn(false);
        when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> {
            Author author = invocation.getArgument(0);
            author.setId(10L);
            return author;
        });

        AuthorResponse response = authorService.createAuthor(request);

        assertEquals(10L, response.getId());
        assertEquals("Nguyễn Nhật Ánh", response.getName());
        assertTrue(response.getActive());
        assertEquals("Nhà văn chuyên viết cho tuổi mới lớn", response.getBio());

        ArgumentCaptor<Author> captor = ArgumentCaptor.forClass(Author.class);
        verify(authorRepository).save(captor.capture());
        assertEquals("Nguyễn Nhật Ánh", captor.getValue().getName());
        assertTrue(captor.getValue().getActive());
        assertEquals("Nhà văn chuyên viết cho tuổi mới lớn", captor.getValue().getBio());
    }

    @Test
    void createAuthor_whenNameExists_throwsAndDoesNotSave() {
        when(authorRepository.existsByNameIgnoreCase("Nguyễn Nhật Ánh")).thenReturn(true);

        Exception exception = assertThrows(Exception.class,
                () -> authorService.createAuthor(request("Nguyễn Nhật Ánh", true, "Tiểu sử")));

        assertEquals("Tên tác giả đã tồn tại", exception.getMessage());
        verify(authorRepository, never()).save(any());
    }

    @Test
    void updateAuthor_whenFoundAndNameFree_updatesFields() throws Exception {
        Author existing = author(1L, "Tên Cũ", true, "Bio cũ");
        when(authorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(authorRepository.existsByNameIgnoreCaseAndIdNot("Tên Mới", 1L)).thenReturn(false);
        when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthorResponse response = authorService.updateAuthor(1L, request("Tên Mới", false, "Bio mới"));

        assertEquals(1L, response.getId());
        assertEquals("Tên Mới", response.getName());
        assertFalse(response.getActive());
        assertEquals("Bio mới", response.getBio());
        assertEquals("Tên Mới", existing.getName());
        assertFalse(existing.getActive());
        assertEquals("Bio mới", existing.getBio());
    }

    @Test
    void updateAuthor_whenMissing_throws() {
        when(authorRepository.findById(99L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class,
                () -> authorService.updateAuthor(99L, request("Tên Mới", true, "Bio")));

        assertEquals("Không tìm thấy tác giả với id = 99", exception.getMessage());
        verify(authorRepository, never()).save(any());
    }

    @Test
    void updateAuthor_whenNameTakenByAnother_throws() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author(1L, "Tên Cũ", true, "Bio cũ")));
        when(authorRepository.existsByNameIgnoreCaseAndIdNot("Tên Trùng", 1L)).thenReturn(true);

        Exception exception = assertThrows(Exception.class,
                () -> authorService.updateAuthor(1L, request("Tên Trùng", true, "Bio")));

        assertEquals("Tên tác giả đã tồn tại", exception.getMessage());
        verify(authorRepository, never()).save(any());
    }

    @Test
    void deleteAuthor_setsActiveFalseAndKeepsBio() throws Exception {
        Author existing = author(4L, "Tác giả X", true, "Bio giữ nguyên");
        when(authorRepository.findById(4L)).thenReturn(Optional.of(existing));
        when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authorService.deleteAuthor(4L);

        assertFalse(existing.getActive());
        assertEquals("Bio giữ nguyên", existing.getBio());
        verify(authorRepository).save(existing);
    }

    @Test
    void deleteAuthor_whenMissing_throws() {
        when(authorRepository.findById(4L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> authorService.deleteAuthor(4L));

        assertEquals("Không tìm thấy tác giả với id = 4", exception.getMessage());
        verify(authorRepository, never()).save(any());
    }

    @Test
    void getAuthorById_returnsMappedResponse() throws Exception {
        when(authorRepository.findById(2L)).thenReturn(Optional.of(author(2L, "Gosho Aoyama", false, "Tác giả Conan")));

        AuthorResponse response = authorService.getAuthorById(2L);

        assertEquals(2L, response.getId());
        assertEquals("Gosho Aoyama", response.getName());
        assertFalse(response.getActive());
        assertEquals("Tác giả Conan", response.getBio());
    }

    @Test
    void getAuthorById_whenMissing_throws() {
        when(authorRepository.findById(2L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> authorService.getAuthorById(2L));

        assertEquals("Không tìm thấy tác giả với id = 2", exception.getMessage());
    }

    @Test
    void getAllAuthors_mapsPageIncludingBio() {
        Pageable pageable = PageRequest.of(0, 20);
        when(authorRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(
                author(1L, "Oda Eiichiro", true, "Tác giả One Piece"),
                author(2L, "Kishimoto", false, "Tác giả Naruto")
        ), pageable, 2));

        Page<AuthorResponse> page = authorService.getAllAuthors(pageable);

        assertEquals(2, page.getTotalElements());
        assertEquals("Oda Eiichiro", page.getContent().get(0).getName());
        assertEquals("Tác giả One Piece", page.getContent().get(0).getBio());
        assertEquals("Kishimoto", page.getContent().get(1).getName());
        assertFalse(page.getContent().get(1).getActive());
    }

    @Test
    void searchAuthors_whenKeywordBlank_usesFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        when(authorRepository.findAll(pageable)).thenReturn(Page.empty(pageable));

        authorService.searchAuthors(null, pageable);
        authorService.searchAuthors("   ", pageable);

        verify(authorRepository, times(2)).findAll(pageable);
        verify(authorRepository, never()).findByNameContainingIgnoreCase(anyString(), eq(pageable));
    }

    @Test
    void searchAuthors_trimsKeywordBeforeQuery() {
        Pageable pageable = PageRequest.of(0, 5);
        when(authorRepository.findByNameContainingIgnoreCase(eq("oda"), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(author(3L, "Oda Eiichiro", true, "One Piece")), pageable, 1));

        Page<AuthorResponse> page = authorService.searchAuthors("  oda  ", pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals("Oda Eiichiro", page.getContent().get(0).getName());
        assertEquals("One Piece", page.getContent().get(0).getBio());
        verify(authorRepository).findByNameContainingIgnoreCase("oda", pageable);
    }

    @Test
    void getAuthorByActiveTrue_mapsActiveList() {
        when(authorRepository.findByActiveTrue())
                .thenReturn(List.of(author(8L, "Tô Hoài", true, "Dế Mèn Phiêu Lưu Ký")));

        List<AuthorResponse> result = authorService.getAuthorByActiveTrue();

        assertEquals(1, result.size());
        assertEquals("Tô Hoài", result.get(0).getName());
        assertTrue(result.get(0).getActive());
        assertEquals("Dế Mèn Phiêu Lưu Ký", result.get(0).getBio());
    }

    @Test
    void searchAuthorByActiveTrue_whenKeywordBlank_returnsAllActive() {
        when(authorRepository.findByActiveTrue())
                .thenReturn(List.of(author(1L, "A", true, "Bio A")));

        assertEquals(1, authorService.searchAuthorByActiveTrue(null).size());
        assertEquals(1, authorService.searchAuthorByActiveTrue("   ").size());

        verify(authorRepository, times(2)).findByActiveTrue();
        verify(authorRepository, never()).findByNameContainingIgnoreCaseAndActiveTrue(anyString());
    }

    @Test
    void searchAuthorByActiveTrue_trimsKeywordBeforeQuery() {
        when(authorRepository.findByNameContainingIgnoreCaseAndActiveTrue("oda"))
                .thenReturn(List.of(author(3L, "Oda Eiichiro", true, "One Piece")));

        List<AuthorResponse> result = authorService.searchAuthorByActiveTrue("  oda  ");

        assertEquals(1, result.size());
        assertEquals("Oda Eiichiro", result.get(0).getName());
        verify(authorRepository).findByNameContainingIgnoreCaseAndActiveTrue("oda");
    }

    private static AuthorRequest request(String name, Boolean active, String bio) {
        AuthorRequest request = new AuthorRequest();
        request.setName(name);
        request.setActive(active);
        request.setBio(bio);
        return request;
    }

    private static Author author(Long id, String name, Boolean active, String bio) {
        Author author = new Author();
        author.setId(id);
        author.setName(name);
        author.setActive(active);
        author.setBio(bio);
        return author;
    }
}