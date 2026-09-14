package com.placement.dto.auth;

import com.placement.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(max = 50)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    // The fields below are only required when role = STUDENT. They create the
    // student's profile record in the same step as their login, so a placement
    // officer or administrator has everything needed to review the request.
    // Left null/blank for ADMIN or PLACEMENT_OFFICER registrations.
    private String fullName;
    private String phone;
    private String rollNumber;
    private Long departmentId;
    private Double cgpa;
    private Integer graduationYear;
    private String resumeLink;
}
