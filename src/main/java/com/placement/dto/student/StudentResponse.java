package com.placement.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String rollNumber;
    private Long departmentId;
    private String departmentName;
    private Double cgpa;
    private Integer graduationYear;
    private String resumeLink;
    // Present only if this record is linked to a self-registered login.
    private Long userId;
}
