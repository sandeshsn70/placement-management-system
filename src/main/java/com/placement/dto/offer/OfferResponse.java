package com.placement.dto.offer;

import com.placement.entity.OfferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferResponse {
    private Long id;
    private Long applicationId;
    private Double ctc;
    private LocalDate offerDate;
    private LocalDate joiningDate;
    private OfferStatus status;
}
