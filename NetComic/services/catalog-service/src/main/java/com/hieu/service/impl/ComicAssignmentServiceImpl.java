package com.hieu.service.impl;

import com.hieu.embeddable.ComicAssignmentId;
import com.hieu.mapper.ComicAssignmentMapper;
import com.hieu.model.Comic;
import com.hieu.model.ComicAssignment;
import com.hieu.payload.request.catalog.ComicAssignmentRequest;
import com.hieu.payload.request.catalog.ReassignStaffRequest;
import com.hieu.payload.response.catalog.ComicAssignmentResponse;
import com.hieu.repository.ComicAssignmentRepository;
import com.hieu.repository.ComicRepository;
import com.hieu.service.ComicAssignmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComicAssignmentServiceImpl implements ComicAssignmentService {

    private final ComicAssignmentRepository assignmentRepository;
    private final ComicRepository comicRepository;

    public ComicAssignmentServiceImpl(ComicAssignmentRepository assignmentRepository,
                                      ComicRepository comicRepository) {
        this.assignmentRepository = assignmentRepository;
        this.comicRepository = comicRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ComicAssignmentResponse createComicAssignment(ComicAssignmentRequest request) throws Exception {
        Comic comic = comicRepository.findById(request.getComicId()).orElseThrow(
                () -> new Exception("Không tìm thấy truyện với id = " + request.getComicId())
        );

        if (assignmentRepository.existsById_ComicIdAndId_StaffId(request.getComicId(), request.getStaffId())) {
            throw new Exception("Nhân viên đã được phân công cho truyện này");
        }

        ComicAssignment saved = assignmentRepository.save(ComicAssignmentMapper.toEntity(request, comic));
        return ComicAssignmentMapper.toResponse(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ComicAssignmentResponse updateComicAssignment(Long comicId, Long staffId, ReassignStaffRequest request)
            throws Exception {
        ComicAssignment current = findOrThrow(comicId, staffId);
        Long newStaffId = request.getNewStaffId();

        if (newStaffId.equals(staffId)) {
            return ComicAssignmentMapper.toResponse(current);
        }
        if (assignmentRepository.existsById_ComicIdAndId_StaffId(comicId, newStaffId)) {
            throw new Exception("Nhân viên mới đã được phân công cho truyện này");
        }

        // Staff là một phần của khóa chính nên "sửa" = xóa dòng cũ rồi thêm dòng mới
        Comic comic = current.getComic();
        assignmentRepository.delete(current);
        ComicAssignment moved = assignmentRepository.save(new ComicAssignment(comic, newStaffId));
        return ComicAssignmentMapper.toResponse(moved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComicAssignment(Long comicId, Long staffId) throws Exception {
        assignmentRepository.delete(findOrThrow(comicId, staffId));
    }

    @Override
    @Transactional(readOnly = true)
    public ComicAssignmentResponse getComicAssignment(Long comicId, Long staffId) throws Exception {
        return ComicAssignmentMapper.toResponse(findOrThrow(comicId, staffId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComicAssignmentResponse> getAllComicAssignments(Pageable pageable) {
        return assignmentRepository.findAll(pageable).map(ComicAssignmentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComicAssignmentResponse> getAssignmentsByComic(Long comicId, Pageable pageable) throws Exception {
        if (!comicRepository.existsById(comicId)) {
            throw new Exception("Không tìm thấy truyện với id = " + comicId);
        }
        return assignmentRepository.findById_ComicId(comicId, pageable).map(ComicAssignmentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComicAssignmentResponse> getAssignmentsByStaff(Long staffId, Pageable pageable) {
        // Không kiểm tra staffId tồn tại: tài khoản nằm ở Identity Service
        return assignmentRepository.findById_StaffId(staffId, pageable).map(ComicAssignmentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isStaffAssigned(Long comicId, Long staffId) {
        return assignmentRepository.existsById_ComicIdAndId_StaffId(comicId, staffId);
    }

    private ComicAssignment findOrThrow(Long comicId, Long staffId) throws Exception {
        return assignmentRepository.findById(new ComicAssignmentId(comicId, staffId)).orElseThrow(
                () -> new Exception("Không tìm thấy phân công: truyện " + comicId + ", nhân viên " + staffId)
        );
    }
}
