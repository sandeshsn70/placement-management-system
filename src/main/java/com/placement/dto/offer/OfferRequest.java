package com.placement.dto.offer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferRequest {

    @NotNull(message = "Application id is required")
    private Long applicationId;

    @NotNull(message = "CTC is required")
    @PositiveOrZero(message = "CTC cannot be negative")
    private Double ctc;

    private LocalDate joiningDate;
}
