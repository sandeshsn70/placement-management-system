package com.placement.controller;

import com.placement.dto.offer.OfferRequest;
import com.placement.dto.offer.OfferResponse;
import com.placement.dto.offer.OfferStatusUpdateRequest;
import com.placement.service.OfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;

    @PostMapping
    public ResponseEntity<OfferResponse> create(@Valid @RequestBody OfferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(offerService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfferResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(offerService.getById(id));
    }

    @GetMapping
    public ResponseEntity<OfferResponse> getByApplication(@RequestParam Long applicationId) {
        return ResponseEntity.ok(offerService.getByApplication(applicationId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OfferResponse> updateStatus(@PathVariable Long id,
                                                        @Valid @RequestBody OfferStatusUpdateRequest request) {
        return ResponseEntity.ok(offerService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        offerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
