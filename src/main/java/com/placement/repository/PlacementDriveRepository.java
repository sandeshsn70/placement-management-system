package com.placement.repository;

import com.placement.entity.PlacementDrive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlacementDriveRepository extends JpaRepository<PlacementDrive, Long> {
    Page<PlacementDrive> findByCompanyId(Long companyId, Pageable pageable);
}
