package com.placement.dto.placementdrive;

import com.placement.entity.DriveStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class PlacementDriveRequest {

    @NotBlank(message = "Drive name is required")
    @Size(max = 150)
    private String name;

    @NotNull(message = "Company id is required")
    private Long companyId;

    @NotNull(message = "Drive date is required")
    private LocalDate driveDate;

    private DriveStatus status;

    @Size(max = 1000)
    private String description;
}
