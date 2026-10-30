package com.hieu.service.impl;

import com.hieu.embeddable.ComicAssignmentId;
import com.hieu.model.Comic;
import com.hieu.model.ComicAssignment;
import com.hieu.payload.request.catalog.ComicAssignmentRequest;
import com.hieu.payload.request.catalog.ReassignStaffRequest;
import com.hieu.payload.response.catalog.ComicAssignmentResponse;
import com.hieu.repository.ComicAssignmentRepository;
import com.hieu.repository.ComicRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComicAssignmentServiceImplTest {

    @Mock
    private ComicAssignmentRepository assignmentRepository;

    @Mock
    private ComicRepository comicRepository;

    @InjectMocks
    private ComicAssignmentServiceImpl service;

    // ---------- create ----------

    @Test
    void createComicAssignment_whenValid_savesAndReturnsResponse() throws Exception {
        Comic comic = comic(1L, "One Piece");
        when(comicRepository.findById(1L)).thenReturn(Optional.of(comic));
        when(assignmentRepository.existsById_ComicIdAndId_StaffId(1L, 7L)).thenReturn(false);
        when(assignmentRepository.save(any(ComicAssignment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ComicAssignmentResponse response = service.createComicAssignment(request(1L, 7L));

        assertEquals(1L, response.getComicId());
        assertEquals("One Piece", response.getComicTitle());
        assertEquals(7L, response.getStaffId());

        ArgumentCaptor<ComicAssignment> captor = ArgumentCaptor.forClass(ComicAssignment.class);
        verify(assignmentRepository).save(captor.capture());
        assertEquals(7L, captor.getValue().getStaffId());
        assertEquals(comic, captor.getValue().getComic());
    }

    @Test
    void createComicAssignment_whenComicNotFound_throwsAndDoesNotSave() {
        when(comicRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> service.createComicAssignment(request(1L, 7L)));

        verify(assignmentRepository, never()).save(any(ComicAssignment.class));
    }

    @Test
    void createComicAssignment_whenAlreadyAssigned_throwsAndDoesNotSave() {
        when(comicRepository.findById(1L)).thenReturn(Optional.of(comic(1L, "One Piece")));
        when(assignmentRepository.existsById_ComicIdAndId_StaffId(1L, 7L)).thenReturn(true);

        assertThrows(Exception.class, () -> service.createComicAssignment(request(1L, 7L)));

        verify(assignmentRepository, never()).save(any(ComicAssignment.class));
    }

    // ---------- update (đổi nhân viên) ----------

    @Test
    void updateComicAssignment_whenNewStaffFree_deletesOldAndSavesNew() throws Exception {
        ComicAssignment current = new ComicAssignment(comic(1L, "One Piece"), 7L);
        when(assignmentRepository.findById(any(ComicAssignmentId.class))).thenReturn(Optional.of(current));
        when(assignmentRepository.existsById_ComicIdAndId_StaffId(1L, 9L)).thenReturn(false);
        when(assignmentRepository.save(any(ComicAssignment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ComicAssignmentResponse response = service.updateComicAssignment(1L, 7L, reassign(9L));

        assertEquals(1L, response.getComicId());
        assertEquals(9L, response.getStaffId());
        verify(assignmentRepository).delete(current);

        ArgumentCaptor<ComicAssignment> captor = ArgumentCaptor.forClass(ComicAssignment.class);
        verify(assignmentRepository).save(captor.capture());
        assertEquals(9L, captor.getValue().getStaffId());
    }

    @Test
    void updateComicAssignment_whenSameStaff_returnsCurrentWithoutChanges() throws Exception {
        ComicAssignment current = new ComicAssignment(comic(1L, "One Piece"), 7L);
        when(assignmentRepository.findById(any(ComicAssignmentId.class))).thenReturn(Optional.of(current));

        ComicAssignmentResponse response = service.updateComicAssignment(1L, 7L, reassign(7L));

        assertEquals(7L, response.getStaffId());
        verify(assignmentRepository, never()).delete(any(ComicAssignment.class));
        verify(assignmentRepository, never()).save(any(ComicAssignment.class));
    }

    @Test
    void updateComicAssignment_whenNewStaffAlreadyAssigned_throwsAndDoesNotDelete() {
        ComicAssignment current = new ComicAssignment(comic(1L, "One Piece"), 7L);
        when(assignmentRepository.findById(any(ComicAssignmentId.class))).thenReturn(Optional.of(current));
        when(assignmentRepository.existsById_ComicIdAndId_StaffId(1L, 9L)).thenReturn(true);

        assertThrows(Exception.class, () -> service.updateComicAssignment(1L, 7L, reassign(9L)));

        verify(assignmentRepository, never()).delete(any(ComicAssignment.class));
        verify(assignmentRepository, never()).save(any(ComicAssignment.class));
    }

    @Test
    void updateComicAssignment_whenNotFound_throws() {
        when(assignmentRepository.findById(any(ComicAssignmentId.class))).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> service.updateComicAssignment(1L, 7L, reassign(9L)));

        verify(assignmentRepository, never()).save(any(ComicAssignment.class));
    }

    // ---------- delete ----------

    @Test
    void deleteComicAssignment_whenFound_deletesRow() throws Exception {
        ComicAssignment current = new ComicAssignment(comic(1L, "One Piece"), 7L);
        when(assignmentRepository.findById(any(ComicAssignmentId.class))).thenReturn(Optional.of(current));

        service.deleteComicAssignment(1L, 7L);

        verify(assignmentRepository).delete(current);
    }

    @Test
    void deleteComicAssignment_whenNotFound_throwsAndDoesNotDelete() {
        when(assignmentRepository.findById(any(ComicAssignmentId.class))).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> service.deleteComicAssignment(1L, 7L));

        verify(assignmentRepository, never()).delete(any(ComicAssignment.class));
    }

    // ---------- read ----------

    @Test
    void getComicAssignment_whenFound_returnsResponse() throws Exception {
        when(assignmentRepository.findById(any(ComicAssignmentId.class)))
                .thenReturn(Optional.of(new ComicAssignment(comic(1L, "One Piece"), 7L)));

        ComicAssignmentResponse response = service.getComicAssignment(1L, 7L);

        assertEquals(1L, response.getComicId());
        assertEquals("One Piece", response.getComicTitle());
        assertEquals(7L, response.getStaffId());
    }

    @Test
    void getComicAssignment_whenNotFound_throws() {
        when(assignmentRepository.findById(any(ComicAssignmentId.class))).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> service.getComicAssignment(1L, 7L));
    }

    @Test
    void getAllComicAssignments_mapsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Comic comic = comic(1L, "One Piece");
        when(assignmentRepository.findAll(pageable)).thenReturn(new PageImpl<>(
                List.of(new ComicAssignment(comic, 7L), new ComicAssignment(comic, 8L)), pageable, 2));

        Page<ComicAssignmentResponse> page = service.getAllComicAssignments(pageable);

        assertEquals(2, page.getTotalElements());
        assertEquals(7L, page.getContent().get(0).getStaffId());
        assertEquals(8L, page.getContent().get(1).getStaffId());
    }

    @Test
    void getAssignmentsByComic_whenComicExists_returnsItsStaff() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        when(comicRepository.existsById(1L)).thenReturn(true);
        when(assignmentRepository.findById_ComicId(eq(1L), eq(pageable))).thenReturn(new PageImpl<>(
                List.of(new ComicAssignment(comic(1L, "One Piece"), 7L)), pageable, 1));

        Page<ComicAssignmentResponse> page = service.getAssignmentsByComic(1L, pageable);

        assertEquals(1, page.getContent().size());
        assertEquals(7L, page.getContent().get(0).getStaffId());
    }

    @Test
    void getAssignmentsByComic_whenComicMissing_throwsAndDoesNotQuery() {
        Pageable pageable = PageRequest.of(0, 10);
        when(comicRepository.existsById(5L)).thenReturn(false);

        assertThrows(Exception.class, () -> service.getAssignmentsByComic(5L, pageable));

        verify(assignmentRepository, never()).findById_ComicId(any(), any());
    }

    @Test
    void getAssignmentsByStaff_returnsComicsOfStaff() {
        Pageable pageable = PageRequest.of(0, 10);
        when(assignmentRepository.findById_StaffId(eq(7L), eq(pageable))).thenReturn(new PageImpl<>(
                List.of(new ComicAssignment(comic(1L, "One Piece"), 7L),
                        new ComicAssignment(comic(2L, "Conan"), 7L)), pageable, 2));

        Page<ComicAssignmentResponse> page = service.getAssignmentsByStaff(7L, pageable);

        assertEquals(2, page.getContent().size());
        assertEquals("Conan", page.getContent().get(1).getComicTitle());
    }

    @Test
    void isStaffAssigned_delegatesToRepository() {
        when(assignmentRepository.existsById_ComicIdAndId_StaffId(1L, 7L)).thenReturn(true);
        when(assignmentRepository.existsById_ComicIdAndId_StaffId(1L, 8L)).thenReturn(false);

        assertTrue(service.isStaffAssigned(1L, 7L));
        assertFalse(service.isStaffAssigned(1L, 8L));
    }

    // ---------- helpers ----------

    private static Comic comic(Long id, String title) {
        Comic comic = new Comic();
        comic.setId(id);
        comic.setTitle(title);
        return comic;
    }

    private static ComicAssignmentRequest request(Long comicId, Long staffId) {
        ComicAssignmentRequest request = new ComicAssignmentRequest();
        request.setComicId(comicId);
        request.setStaffId(staffId);
        return request;
    }

    private static ReassignStaffRequest reassign(Long newStaffId) {
        ReassignStaffRequest request = new ReassignStaffRequest();
        request.setNewStaffId(newStaffId);
        return request;
    }
}
