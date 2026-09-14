package com.placement.repository;

import com.placement.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);
    Optional<Student> findByRollNumber(String rollNumber);
    Optional<Student> findByUserId(Long userId);
    boolean existsByEmail(String email);
    boolean existsByRollNumber(String rollNumber);
    Page<Student> findByDepartmentId(Long departmentId, Pageable pageable);
}
