package com.placement.controller;

import com.placement.dto.application.ApplicationRequest;
import com.placement.dto.application.ApplicationResponse;
import com.placement.dto.application.ApplicationStatusUpdateRequest;
import com.placement.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<ApplicationResponse> apply(@Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.apply(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<ApplicationResponse>> getAll(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long jobId,
            Pageable pageable) {
        if (studentId != null) {
            return ResponseEntity.ok(applicationService.getByStudent(studentId, pageable));
        }
        if (jobId != null) {
            return ResponseEntity.ok(applicationService.getByJob(jobId, pageable));
        }
        return ResponseEntity.ok(applicationService.getAll(pageable));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(@PathVariable Long id,
                                                              @Valid @RequestBody ApplicationStatusUpdateRequest request) {
        return ResponseEntity.ok(applicationService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        applicationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
