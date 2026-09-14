package com.placement.dto.job;

import com.placement.entity.JobType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {
    private Long id;
    private String title;
    private String description;
    private Long companyId;
    private String companyName;
    private Long placementDriveId;
    private Double minCgpa;
    private Double ctc;
    private String location;
    private JobType jobType;
    private LocalDate postedDate;
    private LocalDate deadline;
}
