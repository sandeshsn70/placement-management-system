package com.placement.service;

import com.placement.dto.application.ApplicationRequest;
import com.placement.dto.application.ApplicationResponse;
import com.placement.dto.application.ApplicationStatusUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApplicationService {
    ApplicationResponse apply(ApplicationRequest request);
    ApplicationResponse getById(Long id);
    Page<ApplicationResponse> getAll(Pageable pageable);
    Page<ApplicationResponse> getByStudent(Long studentId, Pageable pageable);
    Page<ApplicationResponse> getByJob(Long jobId, Pageable pageable);
    ApplicationResponse updateStatus(Long id, ApplicationStatusUpdateRequest request);
    void delete(Long id);
}
