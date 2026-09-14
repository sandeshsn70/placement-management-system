package com.placement.controller;

import com.placement.dto.placementdrive.PlacementDriveRequest;
import com.placement.dto.placementdrive.PlacementDriveResponse;
import com.placement.service.PlacementDriveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/placement-drives")
@RequiredArgsConstructor
public class PlacementDriveController {

    private final PlacementDriveService placementDriveService;

    @PostMapping
    public ResponseEntity<PlacementDriveResponse> create(@Valid @RequestBody PlacementDriveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(placementDriveService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlacementDriveResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(placementDriveService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PlacementDriveResponse>> getAll(
            @RequestParam(required = false) Long companyId,
            Pageable pageable) {
        if (companyId != null) {
            return ResponseEntity.ok(placementDriveService.getByCompany(companyId, pageable));
        }
        return ResponseEntity.ok(placementDriveService.getAll(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlacementDriveResponse> update(@PathVariable Long id, @Valid @RequestBody PlacementDriveRequest request) {
        return ResponseEntity.ok(placementDriveService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        placementDriveService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
