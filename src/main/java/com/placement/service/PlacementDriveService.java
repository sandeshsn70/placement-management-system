package com.placement.service;

import com.placement.dto.placementdrive.PlacementDriveRequest;
import com.placement.dto.placementdrive.PlacementDriveResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PlacementDriveService {
    PlacementDriveResponse create(PlacementDriveRequest request);
    PlacementDriveResponse getById(Long id);
    Page<PlacementDriveResponse> getAll(Pageable pageable);
    Page<PlacementDriveResponse> getByCompany(Long companyId, Pageable pageable);
    PlacementDriveResponse update(Long id, PlacementDriveRequest request);
    void delete(Long id);
}
