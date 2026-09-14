package com.placement.service.impl;

import com.placement.dto.placementdrive.PlacementDriveRequest;
import com.placement.dto.placementdrive.PlacementDriveResponse;
import com.placement.entity.Company;
import com.placement.entity.DriveStatus;
import com.placement.entity.PlacementDrive;
import com.placement.exception.ResourceNotFoundException;
import com.placement.repository.CompanyRepository;
import com.placement.repository.PlacementDriveRepository;
import com.placement.service.PlacementDriveService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PlacementDriveServiceImpl implements PlacementDriveService {

    private final PlacementDriveRepository placementDriveRepository;
    private final CompanyRepository companyRepository;

    @Override
    public PlacementDriveResponse create(PlacementDriveRequest request) {
        Company company = findCompany(request.getCompanyId());

        PlacementDrive drive = PlacementDrive.builder()
                .name(request.getName())
                .company(company)
                .driveDate(request.getDriveDate())
                .status(request.getStatus() != null ? request.getStatus() : DriveStatus.UPCOMING)
                .description(request.getDescription())
                .build();

        return toResponse(placementDriveRepository.save(drive));
    }

    @Override
    @Transactional(readOnly = true)
    public PlacementDriveResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PlacementDriveResponse> getAll(Pageable pageable) {
        return placementDriveRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PlacementDriveResponse> getByCompany(Long companyId, Pageable pageable) {
        return placementDriveRepository.findByCompanyId(companyId, pageable).map(this::toResponse);
    }

    @Override
    public PlacementDriveResponse update(Long id, PlacementDriveRequest request) {
        PlacementDrive drive = findEntity(id);
        Company company = findCompany(request.getCompanyId());

        drive.setName(request.getName());
        drive.setCompany(company);
        drive.setDriveDate(request.getDriveDate());
        if (request.getStatus() != null) {
            drive.setStatus(request.getStatus());
        }
        drive.setDescription(request.getDescription());

        return toResponse(placementDriveRepository.save(drive));
    }

    @Override
    public void delete(Long id) {
        placementDriveRepository.delete(findEntity(id));
    }

    private PlacementDrive findEntity(Long id) {
        return placementDriveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Placement drive not found with id: " + id));
    }

    private Company findCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
    }

    private PlacementDriveResponse toResponse(PlacementDrive drive) {
        return PlacementDriveResponse.builder()
                .id(drive.getId())
                .name(drive.getName())
                .companyId(drive.getCompany().getId())
                .companyName(drive.getCompany().getName())
                .driveDate(drive.getDriveDate())
                .status(drive.getStatus())
                .description(drive.getDescription())
                .build();
    }
}
