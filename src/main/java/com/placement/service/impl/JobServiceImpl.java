package com.placement.service.impl;

import com.placement.dto.job.JobRequest;
import com.placement.dto.job.JobResponse;
import com.placement.entity.Company;
import com.placement.entity.Job;
import com.placement.entity.JobType;
import com.placement.entity.PlacementDrive;
import com.placement.exception.ResourceNotFoundException;
import com.placement.repository.CompanyRepository;
import com.placement.repository.JobRepository;
import com.placement.repository.PlacementDriveRepository;
import com.placement.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final PlacementDriveRepository placementDriveRepository;

    @Override
    public JobResponse create(JobRequest request) {
        Company company = findCompany(request.getCompanyId());
        PlacementDrive drive = request.getPlacementDriveId() != null
                ? findDrive(request.getPlacementDriveId())
                : null;

        Job job = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .company(company)
                .placementDrive(drive)
                .minCgpa(request.getMinCgpa())
                .ctc(request.getCtc())
                .location(request.getLocation())
                .jobType(request.getJobType())
                .postedDate(request.getPostedDate())
                .deadline(request.getDeadline())
                .build();

        return toResponse(jobRepository.save(job));
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobResponse> getAll(Pageable pageable) {
        return jobRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobResponse> getByCompany(Long companyId, Pageable pageable) {
        return jobRepository.findByCompanyId(companyId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobResponse> getByType(JobType jobType, Pageable pageable) {
        return jobRepository.findByJobType(jobType, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobResponse> getEligible(Double studentCgpa, Pageable pageable) {
        return jobRepository.findByMinCgpaLessThanEqual(studentCgpa, pageable).map(this::toResponse);
    }

    @Override
    public JobResponse update(Long id, JobRequest request) {
        Job job = findEntity(id);
        Company company = findCompany(request.getCompanyId());
        PlacementDrive drive = request.getPlacementDriveId() != null
                ? findDrive(request.getPlacementDriveId())
                : null;

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setCompany(company);
        job.setPlacementDrive(drive);
        job.setMinCgpa(request.getMinCgpa());
        job.setCtc(request.getCtc());
        job.setLocation(request.getLocation());
        job.setJobType(request.getJobType());
        if (request.getPostedDate() != null) {
            job.setPostedDate(request.getPostedDate());
        }
        job.setDeadline(request.getDeadline());

        return toResponse(jobRepository.save(job));
    }

    @Override
    public void delete(Long id) {
        jobRepository.delete(findEntity(id));
    }

    private Job findEntity(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
    }

    private Company findCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
    }

    private PlacementDrive findDrive(Long driveId) {
        return placementDriveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException("Placement drive not found with id: " + driveId));
    }

    private JobResponse toResponse(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .companyId(job.getCompany().getId())
                .companyName(job.getCompany().getName())
                .placementDriveId(job.getPlacementDrive() != null ? job.getPlacementDrive().getId() : null)
                .minCgpa(job.getMinCgpa())
                .ctc(job.getCtc())
                .location(job.getLocation())
                .jobType(job.getJobType())
                .postedDate(job.getPostedDate())
                .deadline(job.getDeadline())
                .build();
    }
}
