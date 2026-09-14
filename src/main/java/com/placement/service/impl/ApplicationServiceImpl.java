package com.placement.service.impl;

import com.placement.dto.application.ApplicationRequest;
import com.placement.dto.application.ApplicationResponse;
import com.placement.dto.application.ApplicationStatusUpdateRequest;
import com.placement.entity.Application;
import com.placement.entity.Job;
import com.placement.entity.Student;
import com.placement.exception.DuplicateResourceException;
import com.placement.exception.ResourceNotFoundException;
import com.placement.repository.ApplicationRepository;
import com.placement.repository.JobRepository;
import com.placement.repository.StudentRepository;
import com.placement.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final JobRepository jobRepository;

    @Override
    public ApplicationResponse apply(ApplicationRequest request) {
        if (applicationRepository.existsByStudentIdAndJobId(request.getStudentId(), request.getJobId())) {
            throw new DuplicateResourceException("Student has already applied to this job");
        }

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.getStudentId()));
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + request.getJobId()));

        Application application = Application.builder()
                .student(student)
                .job(job)
                .build();

        return toResponse(applicationRepository.save(application));
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getAll(Pageable pageable) {
        return applicationRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getByStudent(Long studentId, Pageable pageable) {
        return applicationRepository.findByStudentId(studentId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getByJob(Long jobId, Pageable pageable) {
        return applicationRepository.findByJobId(jobId, pageable).map(this::toResponse);
    }

    @Override
    public ApplicationResponse updateStatus(Long id, ApplicationStatusUpdateRequest request) {
        Application application = findEntity(id);
        application.setStatus(request.getStatus());
        return toResponse(applicationRepository.save(application));
    }

    @Override
    public void delete(Long id) {
        applicationRepository.delete(findEntity(id));
    }

    private Application findEntity(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
    }

    private ApplicationResponse toResponse(Application application) {
        return ApplicationResponse.builder()
                .id(application.getId())
                .studentId(application.getStudent().getId())
                .studentName(application.getStudent().getFullName())
                .jobId(application.getJob().getId())
                .jobTitle(application.getJob().getTitle())
                .appliedDate(application.getAppliedDate())
                .status(application.getStatus())
                .build();
    }
}
