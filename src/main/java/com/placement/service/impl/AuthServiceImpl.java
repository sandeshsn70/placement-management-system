package com.placement.service.impl;

import com.placement.dto.auth.AuthResponse;
import com.placement.dto.auth.LoginRequest;
import com.placement.dto.auth.RegisterRequest;
import com.placement.entity.AccountStatus;
import com.placement.entity.Department;
import com.placement.entity.Role;
import com.placement.entity.Student;
import com.placement.entity.User;
import com.placement.exception.BadRequestException;
import com.placement.exception.DuplicateResourceException;
import com.placement.exception.ResourceNotFoundException;
import com.placement.repository.DepartmentRepository;
import com.placement.repository.StudentRepository;
import com.placement.repository.UserRepository;
import com.placement.security.JwtService;
import com.placement.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        boolean isStudent = request.getRole() == Role.STUDENT;

        if (isStudent) {
            validateStudentFields(request);
            if (studentRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("A student record already exists with email: " + request.getEmail());
            }
            if (studentRepository.existsByRollNumber(request.getRollNumber())) {
                throw new DuplicateResourceException("A student record already exists with roll number: " + request.getRollNumber());
            }
        }

        // Students start PENDING and wait for a placement officer or admin to
        // approve them; staff accounts are usable right away.
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(isStudent ? AccountStatus.PENDING : AccountStatus.APPROVED)
                .build();
        userRepository.save(user);

        if (isStudent) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

            Student student = Student.builder()
                    .fullName(request.getFullName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .rollNumber(request.getRollNumber())
                    .department(department)
                    .cgpa(request.getCgpa())
                    .graduationYear(request.getGraduationYear())
                    .resumeLink(request.getResumeLink())
                    .user(user)
                    .build();
            studentRepository.save(student);

            return AuthResponse.builder()
                    .username(user.getUsername())
                    .role(user.getRole())
                    .status(user.getStatus())
                    .message("Your registration has been submitted. A placement officer or administrator "
                            + "needs to approve your account before you can sign in.")
                    .build();
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .status(user.getStatus())
                .message("Account created.")
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .status(user.getStatus())
                .message("Welcome back.")
                .build();
    }

    private void validateStudentFields(RegisterRequest request) {
        if (!StringUtils.hasText(request.getFullName())
                || !StringUtils.hasText(request.getRollNumber())
                || request.getDepartmentId() == null
                || request.getCgpa() == null
                || request.getGraduationYear() == null) {
            throw new BadRequestException(
                    "Full name, roll number, department, CGPA and graduation year are required to register as a student.");
        }
    }
}
