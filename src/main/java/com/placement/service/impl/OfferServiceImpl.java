package com.placement.service.impl;

import com.placement.dto.offer.OfferRequest;
import com.placement.dto.offer.OfferResponse;
import com.placement.dto.offer.OfferStatusUpdateRequest;
import com.placement.entity.Application;
import com.placement.entity.Offer;
import com.placement.exception.DuplicateResourceException;
import com.placement.exception.ResourceNotFoundException;
import com.placement.repository.ApplicationRepository;
import com.placement.repository.OfferRepository;
import com.placement.service.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;
    private final ApplicationRepository applicationRepository;

    @Override
    public OfferResponse create(OfferRequest request) {
        if (offerRepository.existsByApplicationId(request.getApplicationId())) {
            throw new DuplicateResourceException("An offer already exists for this application");
        }

        Application application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + request.getApplicationId()));

        Offer offer = Offer.builder()
                .application(application)
                .ctc(request.getCtc())
                .joiningDate(request.getJoiningDate())
                .build();

        return toResponse(offerRepository.save(offer));
    }

    @Override
    @Transactional(readOnly = true)
    public OfferResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public OfferResponse getByApplication(Long applicationId) {
        return offerRepository.findByApplicationId(applicationId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No offer found for application id: " + applicationId));
    }

    @Override
    public OfferResponse updateStatus(Long id, OfferStatusUpdateRequest request) {
        Offer offer = findEntity(id);
        offer.setStatus(request.getStatus());
        return toResponse(offerRepository.save(offer));
    }

    @Override
    public void delete(Long id) {
        offerRepository.delete(findEntity(id));
    }

    private Offer findEntity(Long id) {
        return offerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + id));
    }

    private OfferResponse toResponse(Offer offer) {
        return OfferResponse.builder()
                .id(offer.getId())
                .applicationId(offer.getApplication().getId())
                .ctc(offer.getCtc())
                .offerDate(offer.getOfferDate())
                .joiningDate(offer.getJoiningDate())
                .status(offer.getStatus())
                .build();
    }
}
