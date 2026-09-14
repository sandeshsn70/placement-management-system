package com.placement.service;

import com.placement.dto.offer.OfferRequest;
import com.placement.dto.offer.OfferResponse;
import com.placement.dto.offer.OfferStatusUpdateRequest;

public interface OfferService {
    OfferResponse create(OfferRequest request);
    OfferResponse getById(Long id);
    OfferResponse getByApplication(Long applicationId);
    OfferResponse updateStatus(Long id, OfferStatusUpdateRequest request);
    void delete(Long id);
}
