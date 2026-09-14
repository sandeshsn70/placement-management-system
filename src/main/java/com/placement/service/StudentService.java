package com.placement.service;

import com.placement.dto.student.StudentRequest;
import com.placement.dto.student.StudentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudentService {
    StudentResponse create(StudentRequest request);
    StudentResponse getById(Long id);
    StudentResponse getByUsername(String username);
    Page<StudentResponse> getAll(Pageable pageable);
    Page<StudentResponse> getByDepartment(Long departmentId, Pageable pageable);
    StudentResponse update(Long id, StudentRequest request);
    void delete(Long id);
}
