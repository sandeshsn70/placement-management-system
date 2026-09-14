package com.placement.dto.placementdrive;

import com.placement.entity.DriveStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacementDriveResponse {
    private Long id;
    private String name;
    private Long companyId;
    private String companyName;
    private LocalDate driveDate;
    private DriveStatus status;
    private String description;
}
