package com.placement.controller;

import com.placement.dto.student.StudentRequest;
import com.placement.dto.student.StudentResponse;
import com.placement.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.create(request));
    }

    // Registered before "/{id}" so the literal "me" segment resolves here
    // rather than being parsed as a numeric id.
    @GetMapping("/me")
    public ResponseEntity<StudentResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(studentService.getByUsername(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<StudentResponse>> getAll(
            @RequestParam(required = false) Long departmentId,
            Pageable pageable) {
        if (departmentId != null) {
            return ResponseEntity.ok(studentService.getByDepartment(departmentId, pageable));
        }
        return ResponseEntity.ok(studentService.getAll(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> update(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(studentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
