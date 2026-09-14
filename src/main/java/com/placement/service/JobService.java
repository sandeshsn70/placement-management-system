package com.placement.service;

import com.placement.dto.job.JobRequest;
import com.placement.dto.job.JobResponse;
import com.placement.entity.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobService {
    JobResponse create(JobRequest request);
    JobResponse getById(Long id);
    Page<JobResponse> getAll(Pageable pageable);
    Page<JobResponse> getByCompany(Long companyId, Pageable pageable);
    Page<JobResponse> getByType(JobType jobType, Pageable pageable);
    Page<JobResponse> getEligible(Double studentCgpa, Pageable pageable);
    JobResponse update(Long id, JobRequest request);
    void delete(Long id);
}
