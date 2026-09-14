package com.placement.dto.interview;

import com.placement.entity.InterviewResult;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewResultRequest {

    @NotNull(message = "Result is required")
    private InterviewResult result;

    @Size(max = 1000)
    private String feedback;
}
