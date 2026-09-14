package com.placement.dto.job;

import com.placement.entity.JobType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRequest {

    @NotBlank(message = "Job title is required")
    @Size(max = 150)
    private String title;

    @Size(max = 2000)
    private String description;

    @NotNull(message = "Company id is required")
    private Long companyId;

    private Long placementDriveId;

    @NotNull(message = "Minimum CGPA is required")
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "10.0")
    private Double minCgpa;

    @NotNull(message = "CTC is required")
    @PositiveOrZero(message = "CTC cannot be negative")
    private Double ctc;

    @Size(max = 100)
    private String location;

    @NotNull(message = "Job type is required")
    private JobType jobType;

    private LocalDate postedDate;

    @NotNull(message = "Application deadline is required")
    @FutureOrPresent(message = "Deadline cannot be in the past")
    private LocalDate deadline;
}
