package com.placement.service;

import com.placement.dto.interview.InterviewRequest;
import com.placement.dto.interview.InterviewResponse;
import com.placement.dto.interview.InterviewResultRequest;

import java.util.List;

public interface InterviewService {
    InterviewResponse schedule(InterviewRequest request);
    InterviewResponse getById(Long id);
    List<InterviewResponse> getByApplication(Long applicationId);
    InterviewResponse recordResult(Long id, InterviewResultRequest request);
    void delete(Long id);
}
