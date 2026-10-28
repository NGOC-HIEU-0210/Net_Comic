package com.hieu.service.impl;

import com.hieu.model.Category;
import com.hieu.payload.request.catalog.CategoryRequest;
import com.hieu.payload.response.catalog.CategoryResponse;
import com.hieu.repository.CategoryRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createCategory_whenNameIsNew_savesNameActiveAndDescription() throws Exception {
        CategoryRequest request = request("Hành động", true, "Truyện hành động");
        when(categoryRepository.existsByName("Hành động")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(10L);
            return category;
        });

        CategoryResponse response = categoryService.createCategory(request);

        assertEquals(10L, response.getId());
        assertEquals("Hành động", response.getName());
        assertTrue(response.getActive());
        assertEquals("Truyện hành động", response.getDescrition());

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        assertEquals("Hành động", captor.getValue().getName());
        assertTrue(captor.getValue().getActive());
        assertEquals("Truyện hành động", captor.getValue().getDescription());
    }

    @Test
    void createCategory_whenNameExists_throwsAndDoesNotSave() {
        when(categoryRepository.existsByName("Hành động")).thenReturn(true);

        Exception exception = assertThrows(Exception.class,
                () -> categoryService.createCategory(request("Hành động", true, "Truyện hành động")));

        assertEquals("Tên danh mục đã tồn tại", exception.getMessage());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_whenFoundAndNameFree_updatesFields() throws Exception {
        Category existing = category(1L, "Cũ", true, "Mô tả cũ");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByNameAndIdNot("Mới", 1L)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response = categoryService.updateCategory(1L, request("Mới", false, "Mô tả mới"));

        assertEquals(1L, response.getId());
        assertEquals("Mới", response.getName());
        assertFalse(response.getActive());
        assertEquals("Mô tả mới", response.getDescrition());
        assertEquals("Mới", existing.getName());
        assertFalse(existing.getActive());
        assertEquals("Mô tả mới", existing.getDescription());
    }

    @Test
    void updateCategory_whenMissing_throws() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class,
                () -> categoryService.updateCategory(99L, request("Mới", true, "Mô tả")));

        assertEquals("Không tìm thấy danh mục với id = 99", exception.getMessage());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_whenNameTakenByAnother_throws() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category(1L, "Cũ", true, "Mô tả cũ")));
        when(categoryRepository.existsByNameAndIdNot("Trùng", 1L)).thenReturn(true);

        Exception exception = assertThrows(Exception.class,
                () -> categoryService.updateCategory(1L, request("Trùng", true, "Mô tả")));

        assertEquals("Tên danh mục đã tồn tại", exception.getMessage());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void deleteCategory_setsActiveFalseAndKeepsDescription() throws Exception {
        Category existing = category(4L, "Xóa", true, "Sắp ẩn");
        when(categoryRepository.findById(4L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        categoryService.deleteCategory(4L);

        assertFalse(existing.getActive());
        assertEquals("Sắp ẩn", existing.getDescription());
        verify(categoryRepository).save(existing);
    }

    @Test
    void deleteCategory_whenMissing_throws() {
        when(categoryRepository.findById(4L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> categoryService.deleteCategory(4L));

        assertEquals("Không tìm thấy danh mục với id = 4", exception.getMessage());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void getCategoryById_returnsMappedResponse() throws Exception {
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category(2L, "Romance", false, "Tình cảm")));

        CategoryResponse response = categoryService.getCategoryById(2L);

        assertEquals(2L, response.getId());
        assertEquals("Romance", response.getName());
        assertFalse(response.getActive());
        assertEquals("Tình cảm", response.getDescrition());
    }

    @Test
    void getCategoryById_whenMissing_throws() {
        when(categoryRepository.findById(2L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> categoryService.getCategoryById(2L));

        assertEquals("Không tìm thấy danh mục với id = 2", exception.getMessage());
    }

    @Test
    void getAllCategories_mapsPageIncludingDescription() {
        Pageable pageable = PageRequest.of(0, 20);
        when(categoryRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(
                category(1L, "A", true, "Mô tả A"),
                category(2L, "B", false, "Mô tả B")
        ), pageable, 2));

        Page<CategoryResponse> page = categoryService.getAllCategories(pageable);

        assertEquals(2, page.getTotalElements());
        assertEquals("A", page.getContent().get(0).getName());
        assertEquals("Mô tả A", page.getContent().get(0).getDescrition());
        assertEquals("B", page.getContent().get(1).getName());
        assertFalse(page.getContent().get(1).getActive());
    }

    @Test
    void searchCategories_whenKeywordBlank_usesFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        when(categoryRepository.findAll(pageable)).thenReturn(Page.empty(pageable));

        categoryService.searchCategories(null, pageable);
        categoryService.searchCategories("   ", pageable);

        verify(categoryRepository, org.mockito.Mockito.times(2)).findAll(pageable);
        verify(categoryRepository, never()).findByNameContainingIgnoreCase(anyString(), eq(pageable));
    }

    @Test
    void searchCategories_trimsKeywordBeforeQuery() {
        Pageable pageable = PageRequest.of(0, 5);
        when(categoryRepository.findByNameContainingIgnoreCase(eq("act"), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(category(3L, "Action", true, "Hành động")), pageable, 1));

        Page<CategoryResponse> page = categoryService.searchCategories("  act  ", pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals("Action", page.getContent().get(0).getName());
        assertEquals("Hành động", page.getContent().get(0).getDescrition());
        verify(categoryRepository).findByNameContainingIgnoreCase("act", pageable);
    }

    @Test
    void getCategoryByActiveTrue_mapsActiveList() {
        when(categoryRepository.findByActiveTrue())
                .thenReturn(List.of(category(8L, "Live", true, "Đang hiện")));

        List<CategoryResponse> result = categoryService.getCategoryByActiveTrue();

        assertEquals(1, result.size());
        assertEquals("Live", result.get(0).getName());
        assertTrue(result.get(0).getActive());
        assertEquals("Đang hiện", result.get(0).getDescrition());
    }

    @Test
    void searchCategoriesByActiveTrue_whenKeywordBlank_returnsAllActive() {
        when(categoryRepository.findByActiveTrue())
                .thenReturn(List.of(category(1L, "A", true, "Mô tả A")));

        assertEquals(1, categoryService.searchCategoriesByActiveTrue(null).size());
        assertEquals(1, categoryService.searchCategoriesByActiveTrue("   ").size());

        verify(categoryRepository, org.mockito.Mockito.times(2)).findByActiveTrue();
        verify(categoryRepository, never()).findByNameContainingIgnoreCaseAndActiveTrue(anyString());
    }

    @Test
    void searchCategoriesByActiveTrue_trimsKeywordBeforeQuery() {
        when(categoryRepository.findByNameContainingIgnoreCaseAndActiveTrue("act"))
                .thenReturn(List.of(category(3L, "Action", true, "Hành động")));

        List<CategoryResponse> result = categoryService.searchCategoriesByActiveTrue("  act  ");

        assertEquals(1, result.size());
        assertEquals("Action", result.get(0).getName());
        verify(categoryRepository).findByNameContainingIgnoreCaseAndActiveTrue("act");
    }

    private static CategoryRequest request(String name, Boolean active, String description) {
        CategoryRequest request = new CategoryRequest();
        request.setName(name);
        request.setActive(active);
        request.setDescription(description);
        return request;
    }

    private static Category category(Long id, String name, Boolean active, String description) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setActive(active);
        category.setDescription(description);
        return category;
    }
}