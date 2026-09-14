package com.placement.service.impl;

import com.placement.dto.application.ApplicationRequest;
import com.placement.dto.application.ApplicationResponse;
import com.placement.entity.Application;
import com.placement.entity.ApplicationStatus;
import com.placement.entity.Job;
import com.placement.entity.Student;
import com.placement.exception.DuplicateResourceException;
import com.placement.repository.ApplicationRepository;
import com.placement.repository.JobRepository;
import com.placement.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    @Test
    void apply_throwsDuplicateResourceException_whenStudentAlreadyAppliedToJob() {
        ApplicationRequest request = ApplicationRequest.builder().studentId(1L).jobId(2L).build();
        when(applicationRepository.existsByStudentIdAndJobId(1L, 2L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> applicationService.apply(request));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void apply_savesApplication_whenStudentAndJobExistAndNoDuplicate() {
        ApplicationRequest request = ApplicationRequest.builder().studentId(1L).jobId(2L).build();
        Student student = Student.builder().id(1L).fullName("Asha Rao").build();
        Job job = Job.builder().id(2L).title("Backend Engineer").build();
        Application saved = Application.builder()
                .id(10L).student(student).job(job).status(ApplicationStatus.APPLIED).build();

        when(applicationRepository.existsByStudentIdAndJobId(1L, 2L)).thenReturn(false);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(jobRepository.findById(2L)).thenReturn(Optional.of(job));
        when(applicationRepository.save(any(Application.class))).thenReturn(saved);

        ApplicationResponse response = applicationService.apply(request);

        assertEquals(10L, response.getId());
        assertEquals(ApplicationStatus.APPLIED, response.getStatus());
    }
}
