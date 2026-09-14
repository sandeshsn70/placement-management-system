package com.placement.service.impl;

import com.placement.dto.company.CompanyRequest;
import com.placement.dto.company.CompanyResponse;
import com.placement.entity.Company;
import com.placement.exception.DuplicateResourceException;
import com.placement.exception.ResourceNotFoundException;
import com.placement.repository.CompanyRepository;
import com.placement.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    @Override
    public CompanyResponse create(CompanyRequest request) {
        if (companyRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Company already exists with name: " + request.getName());
        }

        Company company = Company.builder()
                .name(request.getName())
                .description(request.getDescription())
                .website(request.getWebsite())
                .industry(request.getIndustry())
                .build();

        return toResponse(companyRepository.save(company));
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyResponse> getAll() {
        return companyRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public CompanyResponse update(Long id, CompanyRequest request) {
        Company company = findEntity(id);

        if (!company.getName().equals(request.getName()) && companyRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Company already exists with name: " + request.getName());
        }

        company.setName(request.getName());
        company.setDescription(request.getDescription());
        company.setWebsite(request.getWebsite());
        company.setIndustry(request.getIndustry());

        return toResponse(companyRepository.save(company));
    }

    @Override
    public void delete(Long id) {
        companyRepository.delete(findEntity(id));
    }

    private Company findEntity(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
    }

    private CompanyResponse toResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .description(company.getDescription())
                .website(company.getWebsite())
                .industry(company.getIndustry())
                .createdAt(company.getCreatedAt())
                .build();
    }
}
