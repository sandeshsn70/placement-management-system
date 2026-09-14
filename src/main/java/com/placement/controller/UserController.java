package com.placement.controller;

import com.placement.dto.user.UserResponse;
import com.placement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Registered before "/{id}" below; Spring MVC prefers the literal segment
    // match, so this still resolves correctly ahead of the path variable route.
    @GetMapping("/pending")
    public ResponseEntity<Page<UserResponse>> getPending(Pageable pageable) {
        return ResponseEntity.ok(userService.getPending(pageable));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<UserResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(userService.approve(id));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<UserResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(userService.reject(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(userService.getAll(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
