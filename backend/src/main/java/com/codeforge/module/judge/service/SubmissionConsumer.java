package com.codeforge.module.judge.service;

import com.codeforge.common.enums.ProblemDifficulty;
import com.codeforge.common.enums.SubmissionStatus;
import com.codeforge.common.enums.SubmissionVerdict;
import com.codeforge.module.problem.entity.Problem;
import com.codeforge.module.problem.entity.ProblemTestCase;
import com.codeforge.module.problem.repository.ProblemRepository;
import com.codeforge.module.submission.dto.queue.SubmissionTask;
import com.codeforge.module.submission.entity.Submission;
import com.codeforge.module.submission.repository.SubmissionRepository;
import com.codeforge.module.submission.service.SubmissionQueueService;
import com.codeforge.module.user.entity.User;
import com.codeforge.module.user.entity.UserStats;
import com.codeforge.module.user.repository.UserRepository;
import com.codeforge.module.user.repository.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionConsumer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;
    private final CodeExecutor codeExecutor;
    private final VerdictEvaluator verdictEvaluator;

    /**
     * Polls the Redis queue for pending submissions.
     */
    @Scheduled(fixedDelay = 500)
    public void pollAndProcess() {
        try {
            Object taskObj = redisTemplate.opsForList().rightPop(SubmissionQueueService.SUBMISSION_QUEUE_KEY);
            if (taskObj instanceof SubmissionTask task) {
                processSubmission(task);
            }
        } catch (Exception ex) {
            log.trace("Queue polling error (expected if Redis is not running in local test mode): {}", ex.getMessage());
        }
    }

    @Transactional
    public void processSubmission(SubmissionTask task) {
        log.info("Processing submission #{} for problem #{}", task.getSubmissionId(), task.getProblemId());

        Submission submission = submissionRepository.findByIdWithDetails(task.getSubmissionId()).orElse(null);
        if (submission == null) {
            log.error("Submission #{} not found in database", task.getSubmissionId());
            return;
        }

        submission.setStatus(SubmissionStatus.RUNNING);
        submissionRepository.save(submission);

        Problem problem = problemRepository.findByIdWithTestCases(task.getProblemId()).orElse(null);
        if (problem == null || problem.getTestCases().isEmpty()) {
            submission.setStatus(SubmissionStatus.FAILED);
            submission.setVerdict(SubmissionVerdict.INTERNAL_ERROR);
            submission.setErrorOutput("No test cases found for problem");
            submissionRepository.save(submission);
            return;
        }

        List<ProblemTestCase> testCases = problem.getTestCases();
        SubmissionVerdict finalVerdict = SubmissionVerdict.ACCEPTED;
        int maxExecutionTimeMs = 0;
        int maxMemoryUsedKb = 0;
        String errorOutput = null;

        for (ProblemTestCase testCase : testCases) {
            var result = codeExecutor.execute(
                    task.getLanguage(),
                    task.getSourceCode(),
                    testCase.getInputData(),
                    task.getTimeLimitMs(),
                    task.getMemoryLimitMb()
            );

            maxExecutionTimeMs = Math.max(maxExecutionTimeMs, result.getExecutionTimeMs());
            maxMemoryUsedKb = Math.max(maxMemoryUsedKb, result.getMemoryUsedKb());

            SubmissionVerdict tcVerdict = verdictEvaluator.evaluate(result, testCase.getExpectedOutput());

            if (tcVerdict != SubmissionVerdict.ACCEPTED) {
                finalVerdict = tcVerdict;
                if (result.getStderr() != null && !result.getStderr().isBlank()) {
                    errorOutput = result.getStderr();
                } else if (tcVerdict == SubmissionVerdict.WRONG_ANSWER) {
                    errorOutput = String.format("Failed on test case #%d. Expected: '%s', Actual: '%s'",
                            testCase.getOrderIndex(), testCase.getExpectedOutput(), result.getStdout());
                }
                break; // Short-circuit on first failure
            }
        }

        submission.setStatus(SubmissionStatus.COMPLETED);
        submission.setVerdict(finalVerdict);
        submission.setExecutionTimeMs(maxExecutionTimeMs);
        submission.setMemoryUsedKb(maxMemoryUsedKb);
        submission.setErrorOutput(errorOutput);
        submissionRepository.save(submission);

        // Update User Problem Solving Stats
        updateUserStats(submission.getUser().getId(), problem, finalVerdict);

        log.info("Finished submission #{} with verdict: {}", submission.getId(), finalVerdict);
    }

    private void updateUserStats(Long userId, Problem problem, SubmissionVerdict verdict) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        UserStats stats = user.getStats();
        if (stats == null) {
            stats = UserStats.builder().user(user).build();
        }

        stats.setTotalSubmissions(stats.getTotalSubmissions() + 1);

        if (verdict == SubmissionVerdict.ACCEPTED) {
            stats.setAcceptedSubmissions(stats.getAcceptedSubmissions() + 1);

            // Check if this is the first accepted submission for this problem
            long acceptedCount = submissionRepository.countByUserIdAndProblemIdAndVerdict(
                    userId, problem.getId(), SubmissionVerdict.ACCEPTED
            );

            if (acceptedCount <= 1) {
                if (problem.getDifficulty() == ProblemDifficulty.EASY) {
                    stats.setEasySolved(stats.getEasySolved() + 1);
                } else if (problem.getDifficulty() == ProblemDifficulty.MEDIUM) {
                    stats.setMediumSolved(stats.getMediumSolved() + 1);
                } else if (problem.getDifficulty() == ProblemDifficulty.HARD) {
                    stats.setHardSolved(stats.getHardSolved() + 1);
                }
            }
        }

        userStatsRepository.save(stats);
    }
}
