package com.placement.service.impl;

import com.placement.dto.department.DepartmentRequest;
import com.placement.dto.department.DepartmentResponse;
import com.placement.entity.Department;
import com.placement.exception.DuplicateResourceException;
import com.placement.exception.ResourceNotFoundException;
import com.placement.repository.DepartmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    @Test
    void create_savesDepartment_whenNameAndCodeAreUnique() {
        DepartmentRequest request = DepartmentRequest.builder().name("Computer Engineering").code("COMP").build();
        Department saved = Department.builder().id(1L).name("Computer Engineering").code("COMP").build();

        when(departmentRepository.existsByName("Computer Engineering")).thenReturn(false);
        when(departmentRepository.existsByCode("COMP")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(saved);

        DepartmentResponse response = departmentService.create(request);

        assertEquals(1L, response.getId());
        assertEquals("Computer Engineering", response.getName());
        assertEquals("COMP", response.getCode());
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void create_throwsDuplicateResourceException_whenNameAlreadyExists() {
        DepartmentRequest request = DepartmentRequest.builder().name("Computer Engineering").code("COMP").build();
        when(departmentRepository.existsByName("Computer Engineering")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> departmentService.create(request));
        verify(departmentRepository, never()).save(any());
    }

    @Test
    void getById_throwsResourceNotFoundException_whenDepartmentDoesNotExist() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> departmentService.getById(99L));
    }
}
