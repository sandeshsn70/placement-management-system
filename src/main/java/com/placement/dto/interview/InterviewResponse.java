package com.placement.dto.interview;

import com.placement.entity.InterviewMode;
import com.placement.entity.InterviewResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewResponse {
    private Long id;
    private Long applicationId;
    private Integer round;
    private LocalDateTime interviewDate;
    private InterviewMode mode;
    private String interviewerName;
    private String feedback;
    private InterviewResult result;
}
