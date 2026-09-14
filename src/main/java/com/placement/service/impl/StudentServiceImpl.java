package com.placement.service.impl;

import com.placement.dto.student.StudentRequest;
import com.placement.dto.student.StudentResponse;
import com.placement.entity.Department;
import com.placement.entity.Student;
import com.placement.entity.User;
import com.placement.exception.DuplicateResourceException;
import com.placement.exception.ResourceNotFoundException;
import com.placement.repository.DepartmentRepository;
import com.placement.repository.StudentRepository;
import com.placement.repository.UserRepository;
import com.placement.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    @Override
    public StudentResponse create(StudentRequest request) {
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Student already exists with email: " + request.getEmail());
        }
        if (studentRepository.existsByRollNumber(request.getRollNumber())) {
            throw new DuplicateResourceException("Student already exists with roll number: " + request.getRollNumber());
        }

        Department department = findDepartment(request.getDepartmentId());

        Student student = Student.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .rollNumber(request.getRollNumber())
                .department(department)
                .cgpa(request.getCgpa())
                .graduationYear(request.getGraduationYear())
                .resumeLink(request.getResumeLink())
                .build();

        return toResponse(studentRepository.save(student));
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No student profile is linked to this account."));
        return toResponse(student);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponse> getAll(Pageable pageable) {
        return studentRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponse> getByDepartment(Long departmentId, Pageable pageable) {
        return studentRepository.findByDepartmentId(departmentId, pageable).map(this::toResponse);
    }

    @Override
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = findEntity(id);

        if (!student.getEmail().equals(request.getEmail()) && studentRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Student already exists with email: " + request.getEmail());
        }
        if (!student.getRollNumber().equals(request.getRollNumber()) && studentRepository.existsByRollNumber(request.getRollNumber())) {
            throw new DuplicateResourceException("Student already exists with roll number: " + request.getRollNumber());
        }

        Department department = findDepartment(request.getDepartmentId());

        student.setFullName(request.getFullName());
        student.setEmail(request.getEmail());
        student.setPhone(request.getPhone());
        student.setRollNumber(request.getRollNumber());
        student.setDepartment(department);
        student.setCgpa(request.getCgpa());
        student.setGraduationYear(request.getGraduationYear());
        student.setResumeLink(request.getResumeLink());

        return toResponse(studentRepository.save(student));
    }

    @Override
    public void delete(Long id) {
        studentRepository.delete(findEntity(id));
    }

    private Student findEntity(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    private Department findDepartment(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));
    }

    private StudentResponse toResponse(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .fullName(student.getFullName())
                .email(student.getEmail())
                .phone(student.getPhone())
                .rollNumber(student.getRollNumber())
                .departmentId(student.getDepartment().getId())
                .departmentName(student.getDepartment().getName())
                .cgpa(student.getCgpa())
                .graduationYear(student.getGraduationYear())
                .resumeLink(student.getResumeLink())
                .userId(student.getUser() != null ? student.getUser().getId() : null)
                .build();
    }
}
