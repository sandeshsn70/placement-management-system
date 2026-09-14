package com.placement.service.impl;

import com.placement.dto.interview.InterviewRequest;
import com.placement.dto.interview.InterviewResponse;
import com.placement.dto.interview.InterviewResultRequest;
import com.placement.entity.Application;
import com.placement.entity.Interview;
import com.placement.exception.ResourceNotFoundException;
import com.placement.repository.ApplicationRepository;
import com.placement.repository.InterviewRepository;
import com.placement.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;

    @Override
    public InterviewResponse schedule(InterviewRequest request) {
        Application application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + request.getApplicationId()));

        Interview interview = Interview.builder()
                .application(application)
                .round(request.getRound())
                .interviewDate(request.getInterviewDate())
                .mode(request.getMode())
                .interviewerName(request.getInterviewerName())
                .build();

        return toResponse(interviewRepository.save(interview));
    }

    @Override
    @Transactional(readOnly = true)
    public InterviewResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewResponse> getByApplication(Long applicationId) {
        return interviewRepository.findByApplicationId(applicationId).stream().map(this::toResponse).toList();
    }

    @Override
    public InterviewResponse recordResult(Long id, InterviewResultRequest request) {
        Interview interview = findEntity(id);
        interview.setResult(request.getResult());
        interview.setFeedback(request.getFeedback());
        return toResponse(interviewRepository.save(interview));
    }

    @Override
    public void delete(Long id) {
        interviewRepository.delete(findEntity(id));
    }

    private Interview findEntity(Long id) {
        return interviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found with id: " + id));
    }

    private InterviewResponse toResponse(Interview interview) {
        return InterviewResponse.builder()
                .id(interview.getId())
                .applicationId(interview.getApplication().getId())
                .round(interview.getRound())
                .interviewDate(interview.getInterviewDate())
                .mode(interview.getMode())
                .interviewerName(interview.getInterviewerName())
                .feedback(interview.getFeedback())
                .result(interview.getResult())
                .build();
    }
}
