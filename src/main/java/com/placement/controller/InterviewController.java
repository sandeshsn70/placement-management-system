package com.placement.controller;

import com.placement.dto.interview.InterviewRequest;
import com.placement.dto.interview.InterviewResponse;
import com.placement.dto.interview.InterviewResultRequest;
import com.placement.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping
    public ResponseEntity<InterviewResponse> schedule(@Valid @RequestBody InterviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(interviewService.schedule(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterviewResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(interviewService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<InterviewResponse>> getByApplication(@RequestParam Long applicationId) {
        return ResponseEntity.ok(interviewService.getByApplication(applicationId));
    }

    @PatchMapping("/{id}/result")
    public ResponseEntity<InterviewResponse> recordResult(@PathVariable Long id,
                                                            @Valid @RequestBody InterviewResultRequest request) {
        return ResponseEntity.ok(interviewService.recordResult(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        interviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
