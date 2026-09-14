package com.placement.repository;

import com.placement.entity.Job;
import com.placement.entity.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {
    Page<Job> findByCompanyId(Long companyId, Pageable pageable);
    Page<Job> findByJobType(JobType jobType, Pageable pageable);
    Page<Job> findByMinCgpaLessThanEqual(Double cgpa, Pageable pageable);
}
