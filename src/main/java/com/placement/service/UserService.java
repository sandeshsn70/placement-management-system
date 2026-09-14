package com.placement.service;

import com.placement.dto.user.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse getById(Long id);
    Page<UserResponse> getAll(Pageable pageable);
    Page<UserResponse> getPending(Pageable pageable);
    UserResponse approve(Long id);
    UserResponse reject(Long id);
    void delete(Long id);
}
