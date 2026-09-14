package com.placement.service.impl;

import com.placement.dto.user.UserResponse;
import com.placement.entity.AccountStatus;
import com.placement.entity.User;
import com.placement.exception.ResourceNotFoundException;
import com.placement.repository.UserRepository;
import com.placement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getPending(Pageable pageable) {
        return userRepository.findByStatus(AccountStatus.PENDING, pageable).map(this::toResponse);
    }

    @Override
    public UserResponse approve(Long id) {
        User user = findEntity(id);
        user.setStatus(AccountStatus.APPROVED);
        return toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse reject(Long id) {
        User user = findEntity(id);
        user.setStatus(AccountStatus.REJECTED);
        return toResponse(userRepository.save(user));
    }

    @Override
    public void delete(Long id) {
        userRepository.delete(findEntity(id));
    }

    private User findEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
