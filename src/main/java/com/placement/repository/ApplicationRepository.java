package com.placement.repository;

import com.placement.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Page<Application> findByStudentId(Long studentId, Pageable pageable);
    Page<Application> findByJobId(Long jobId, Pageable pageable);
    boolean existsByStudentIdAndJobId(Long studentId, Long jobId);
    Optional<Application> findByStudentIdAndJobId(Long studentId, Long jobId);
}
