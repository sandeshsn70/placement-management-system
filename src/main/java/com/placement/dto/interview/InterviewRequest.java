package com.placement.dto.interview;

import com.placement.entity.InterviewMode;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewRequest {

    @NotNull(message = "Application id is required")
    private Long applicationId;

    @NotNull(message = "Round is required")
    @Positive(message = "Round must be a positive number")
    private Integer round;

    @NotNull(message = "Interview date is required")
    @Future(message = "Interview date must be in the future")
    private LocalDateTime interviewDate;

    @NotNull(message = "Mode is required")
    private InterviewMode mode;

    @Size(max = 150)
    private String interviewerName;
}
