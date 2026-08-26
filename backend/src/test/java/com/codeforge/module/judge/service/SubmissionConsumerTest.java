package com.codeforge.module.judge.service;

import com.codeforge.common.enums.ProblemDifficulty;
import com.codeforge.common.enums.ProgrammingLanguage;
import com.codeforge.common.enums.SubmissionStatus;
import com.codeforge.common.enums.SubmissionVerdict;
import com.codeforge.module.judge.dto.ExecutionResult;
import com.codeforge.module.problem.entity.Problem;
import com.codeforge.module.problem.entity.ProblemTestCase;
import com.codeforge.module.problem.repository.ProblemRepository;
import com.codeforge.module.submission.dto.queue.SubmissionTask;
import com.codeforge.module.submission.entity.Submission;
import com.codeforge.module.submission.repository.SubmissionRepository;
import com.codeforge.module.user.entity.User;
import com.codeforge.module.user.entity.UserStats;
import com.codeforge.module.user.repository.UserRepository;
import com.codeforge.module.user.repository.UserStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionConsumerTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserStatsRepository userStatsRepository;

    @Mock
    private CodeExecutor codeExecutor;

    @Spy
    private VerdictEvaluator verdictEvaluator = new VerdictEvaluator();

    @InjectMocks
    private SubmissionConsumer submissionConsumer;

    private User sampleUser;
    private Problem sampleProblem;
    private Submission sampleSubmission;
    private UserStats userStats;

    @BeforeEach
    void setUp() {
        userStats = UserStats.builder()
                .userId(1L)
                .easySolved(0)
                .mediumSolved(0)
                .hardSolved(0)
                .totalSubmissions(0)
                .acceptedSubmissions(0)
                .build();

        sampleUser = User.builder()
                .id(1L)
                .username("john_doe")
                .stats(userStats)
                .build();

        ProblemTestCase tc1 = ProblemTestCase.builder()
                .id(10L)
                .inputData("4\n2 7 11 15\n9")
                .expectedOutput("0 1")
                .orderIndex(1)
                .build();

        ProblemTestCase tc2 = ProblemTestCase.builder()
                .id(11L)
                .inputData("3\n3 2 4\n6")
                .expectedOutput("1 2")
                .orderIndex(2)
                .build();

        sampleProblem = Problem.builder()
                .id(100L)
                .slug("two-sum")
                .title("Two Sum")
                .difficulty(ProblemDifficulty.EASY)
                .timeLimitMs(1000)
                .memoryLimitMb(256)
                .testCases(List.of(tc1, tc2))
                .build();

        sampleSubmission = Submission.builder()
                .id(1000L)
                .user(sampleUser)
                .problem(sampleProblem)
                .language(ProgrammingLanguage.JAVA)
                .sourceCode("class Solution {}")
                .status(SubmissionStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Should evaluate submission as ACCEPTED and increment easySolved user stats")
    void shouldProcessAcceptedSubmission() {
        SubmissionTask task = SubmissionTask.builder()
                .submissionId(1000L)
                .problemId(100L)
                .language(ProgrammingLanguage.JAVA)
                .sourceCode("class Solution {}")
                .timeLimitMs(1000)
                .memoryLimitMb(256)
                .build();

        when(submissionRepository.findByIdWithDetails(1000L)).thenReturn(Optional.of(sampleSubmission));
        when(problemRepository.findByIdWithTestCases(100L)).thenReturn(Optional.of(sampleProblem));
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(submissionRepository.countByUserIdAndProblemIdAndVerdict(1L, 100L, SubmissionVerdict.ACCEPTED))
                .thenReturn(1L); // First time solving

        ExecutionResult exec1 = ExecutionResult.builder().exitCode(0).stdout("0 1").executionTimeMs(30).build();
        ExecutionResult exec2 = ExecutionResult.builder().exitCode(0).stdout("1 2").executionTimeMs(35).build();

        when(codeExecutor.execute(eq(ProgrammingLanguage.JAVA), anyString(), eq("4\n2 7 11 15\n9"), eq(1000), eq(256)))
                .thenReturn(exec1);
        when(codeExecutor.execute(eq(ProgrammingLanguage.JAVA), anyString(), eq("3\n3 2 4\n6"), eq(1000), eq(256)))
                .thenReturn(exec2);

        submissionConsumer.processSubmission(task);

        assertEquals(SubmissionStatus.COMPLETED, sampleSubmission.getStatus());
        assertEquals(SubmissionVerdict.ACCEPTED, sampleSubmission.getVerdict());
        assertEquals(35, sampleSubmission.getExecutionTimeMs());

        verify(userStatsRepository, times(1)).save(argThat(stats ->
                stats.getEasySolved() == 1 && stats.getTotalSubmissions() == 1 && stats.getAcceptedSubmissions() == 1
        ));
    }

    @Test
    @DisplayName("Should short-circuit execution on WRONG_ANSWER and not run subsequent test cases")
    void shouldShortCircuitOnFailure() {
        SubmissionTask task = SubmissionTask.builder()
                .submissionId(1000L)
                .problemId(100L)
                .language(ProgrammingLanguage.JAVA)
                .sourceCode("class Solution {}")
                .timeLimitMs(1000)
                .memoryLimitMb(256)
                .build();

        when(submissionRepository.findByIdWithDetails(1000L)).thenReturn(Optional.of(sampleSubmission));
        when(problemRepository.findByIdWithTestCases(100L)).thenReturn(Optional.of(sampleProblem));
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        ExecutionResult exec1 = ExecutionResult.builder().exitCode(0).stdout("99 99").executionTimeMs(20).build(); // Wrong output

        when(codeExecutor.execute(eq(ProgrammingLanguage.JAVA), anyString(), eq("4\n2 7 11 15\n9"), eq(1000), eq(256)))
                .thenReturn(exec1);

        submissionConsumer.processSubmission(task);

        assertEquals(SubmissionStatus.COMPLETED, sampleSubmission.getStatus());
        assertEquals(SubmissionVerdict.WRONG_ANSWER, sampleSubmission.getVerdict());

        // Should NOT execute test case 2
        verify(codeExecutor, times(1)).execute(any(), any(), any(), anyInt(), anyInt());
        verify(userStatsRepository, times(1)).save(argThat(stats ->
                stats.getEasySolved() == 0 && stats.getTotalSubmissions() == 1 && stats.getAcceptedSubmissions() == 0
        ));
    }
}
