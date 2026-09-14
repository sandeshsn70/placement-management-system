package com.placement.service;

import com.placement.dto.department.DepartmentRequest;
import com.placement.dto.department.DepartmentResponse;

import java.util.List;

public interface DepartmentService {
    DepartmentResponse create(DepartmentRequest request);
    DepartmentResponse getById(Long id);
    List<DepartmentResponse> getAll();
    DepartmentResponse update(Long id, DepartmentRequest request);
    void delete(Long id);
}
