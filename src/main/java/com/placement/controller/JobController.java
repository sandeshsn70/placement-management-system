package com.placement.controller;

import com.placement.dto.job.JobRequest;
import com.placement.dto.job.JobResponse;
import com.placement.entity.JobType;
import com.placement.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobResponse> create(@Valid @RequestBody JobRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<JobResponse>> getAll(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) Double maxCgpaRequired,
            Pageable pageable) {
        if (companyId != null) {
            return ResponseEntity.ok(jobService.getByCompany(companyId, pageable));
        }
        if (jobType != null) {
            return ResponseEntity.ok(jobService.getByType(jobType, pageable));
        }
        if (maxCgpaRequired != null) {
            return ResponseEntity.ok(jobService.getEligible(maxCgpaRequired, pageable));
        }
        return ResponseEntity.ok(jobService.getAll(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> update(@PathVariable Long id, @Valid @RequestBody JobRequest request) {
        return ResponseEntity.ok(jobService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        jobService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
