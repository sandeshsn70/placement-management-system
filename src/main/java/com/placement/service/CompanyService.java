package com.placement.service;

import com.placement.dto.company.CompanyRequest;
import com.placement.dto.company.CompanyResponse;

import java.util.List;

public interface CompanyService {
    CompanyResponse create(CompanyRequest request);
    CompanyResponse getById(Long id);
    List<CompanyResponse> getAll();
    CompanyResponse update(Long id, CompanyRequest request);
    void delete(Long id);
}
