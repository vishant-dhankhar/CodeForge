package com.codeforge.module.submission.service;

import com.codeforge.common.dto.PagedResponse;
import com.codeforge.common.enums.ProgrammingLanguage;
import com.codeforge.common.enums.SubmissionStatus;
import com.codeforge.common.enums.SubmissionVerdict;
import com.codeforge.common.exception.ResourceNotFoundException;
import com.codeforge.module.problem.entity.Problem;
import com.codeforge.module.problem.repository.ProblemRepository;
import com.codeforge.module.submission.dto.queue.SubmissionTask;
import com.codeforge.module.submission.dto.request.CreateSubmissionRequest;
import com.codeforge.module.submission.dto.response.SubmissionResponse;
import com.codeforge.module.submission.entity.Submission;
import com.codeforge.module.submission.repository.SubmissionRepository;
import com.codeforge.module.submission.service.impl.SubmissionServiceImpl;
import com.codeforge.module.user.entity.User;
import com.codeforge.module.user.repository.UserRepository;
import com.codeforge.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubmissionQueueService queueService;

    @InjectMocks
    private SubmissionServiceImpl submissionService;

    private User sampleUser;
    private Problem sampleProblem;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .build();

        sampleProblem = Problem.builder()
                .id(10L)
                .slug("two-sum")
                .title("Two Sum")
                .timeLimitMs(1000)
                .memoryLimitMb(256)
                .build();

        userPrincipal = new UserPrincipal(
                1L, "john_doe", "john@example.com", "pass",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    @DisplayName("Should create submission with PENDING status and push to Redis queue")
    void shouldCreateSubmissionAndPushToQueue() {
        CreateSubmissionRequest request = CreateSubmissionRequest.builder()
                .problemId(10L)
                .language(ProgrammingLanguage.JAVA)
                .sourceCode("class Solution {}")
                .build();

        when(userRepository.getReferenceById(1L)).thenReturn(sampleUser);
        when(problemRepository.findById(10L)).thenReturn(Optional.of(sampleProblem));

        Submission savedSubmission = Submission.builder()
                .id(100L)
                .user(sampleUser)
                .problem(sampleProblem)
                .language(ProgrammingLanguage.JAVA)
                .sourceCode("class Solution {}")
                .status(SubmissionStatus.PENDING)
                .build();

        when(submissionRepository.save(any(Submission.class))).thenReturn(savedSubmission);

        SubmissionResponse response = submissionService.createSubmission(userPrincipal, request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(SubmissionStatus.PENDING, response.getStatus());
        assertEquals("two-sum", response.getProblemSlug());
        assertEquals("john_doe", response.getUsername());

        verify(queueService, times(1)).pushTask(any(SubmissionTask.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when problem does not exist")
    void shouldThrowExceptionWhenProblemNotFound() {
        CreateSubmissionRequest request = CreateSubmissionRequest.builder()
                .problemId(999L)
                .language(ProgrammingLanguage.JAVA)
                .sourceCode("class Solution {}")
                .build();

        when(userRepository.getReferenceById(1L)).thenReturn(sampleUser);
        when(problemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                submissionService.createSubmission(userPrincipal, request));
    }

    @Test
    @DisplayName("Should fetch submission details by ID")
    void shouldFetchSubmissionById() {
        Submission submission = Submission.builder()
                .id(100L)
                .user(sampleUser)
                .problem(sampleProblem)
                .language(ProgrammingLanguage.JAVA)
                .sourceCode("class Solution {}")
                .status(SubmissionStatus.COMPLETED)
                .verdict(SubmissionVerdict.ACCEPTED)
                .executionTimeMs(45)
                .memoryUsedKb(14200)
                .build();

        when(submissionRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(submission));

        SubmissionResponse response = submissionService.getSubmissionById(100L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(SubmissionStatus.COMPLETED, response.getStatus());
        assertEquals(SubmissionVerdict.ACCEPTED, response.getVerdict());
        assertEquals(45, response.getExecutionTimeMs());
    }

    @Test
    @DisplayName("Should return paginated user submissions history")
    void shouldReturnUserSubmissionsHistory() {
        Submission submission = Submission.builder()
                .id(100L)
                .user(sampleUser)
                .problem(sampleProblem)
                .language(ProgrammingLanguage.CPP)
                .sourceCode("int main() {}")
                .status(SubmissionStatus.COMPLETED)
                .verdict(SubmissionVerdict.ACCEPTED)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        when(submissionRepository.findByUserId(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(submission), pageable, 1));

        PagedResponse<SubmissionResponse> response = submissionService.getMySubmissions(userPrincipal, null, pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("two-sum", response.getContent().get(0).getProblemSlug());
    }
}
