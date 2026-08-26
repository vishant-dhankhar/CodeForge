package com.codeforge.module.submission.service.impl;

import com.codeforge.common.dto.PagedResponse;
import com.codeforge.common.enums.SubmissionStatus;
import com.codeforge.common.exception.ResourceNotFoundException;
import com.codeforge.module.problem.entity.Problem;
import com.codeforge.module.problem.repository.ProblemRepository;
import com.codeforge.module.submission.dto.queue.SubmissionTask;
import com.codeforge.module.submission.dto.request.CreateSubmissionRequest;
import com.codeforge.module.submission.dto.response.SubmissionResponse;
import com.codeforge.module.submission.entity.Submission;
import com.codeforge.module.submission.repository.SubmissionRepository;
import com.codeforge.module.submission.service.SubmissionQueueService;
import com.codeforge.module.submission.service.SubmissionService;
import com.codeforge.module.user.entity.User;
import com.codeforge.module.user.repository.UserRepository;
import com.codeforge.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final SubmissionQueueService queueService;

    @Override
    @Transactional
    public SubmissionResponse createSubmission(UserPrincipal currentUser, CreateSubmissionRequest request) {
        User user = userRepository.getReferenceById(currentUser.getId());
        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new ResourceNotFoundException("Problem", "id", request.getProblemId()));

        Submission submission = Submission.builder()
                .user(user)
                .problem(problem)
                .language(request.getLanguage())
                .sourceCode(request.getSourceCode())
                .status(SubmissionStatus.PENDING)
                .build();

        Submission savedSubmission = submissionRepository.save(submission);

        // Push task to Redis queue for background execution worker
        SubmissionTask task = SubmissionTask.builder()
                .submissionId(savedSubmission.getId())
                .problemId(problem.getId())
                .language(savedSubmission.getLanguage())
                .sourceCode(savedSubmission.getSourceCode())
                .timeLimitMs(problem.getTimeLimitMs())
                .memoryLimitMb(problem.getMemoryLimitMb())
                .build();

        queueService.pushTask(task);

        return mapToResponse(savedSubmission);
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionResponse getSubmissionById(Long id) {
        Submission submission = submissionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", id));

        return mapToResponse(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SubmissionResponse> getMySubmissions(
            UserPrincipal currentUser, Long problemId, Pageable pageable) {

        Page<Submission> page;
        if (problemId != null) {
            page = submissionRepository.findByUserIdAndProblemId(currentUser.getId(), problemId, pageable);
        } else {
            page = submissionRepository.findByUserId(currentUser.getId(), pageable);
        }

        return PagedResponse.fromPage(page.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SubmissionResponse> getSubmissionsByProblemSlug(String problemSlug, Pageable pageable) {
        Page<Submission> page = submissionRepository.findByProblemSlug(problemSlug, pageable);
        return PagedResponse.fromPage(page.map(this::mapToResponse));
    }

    private SubmissionResponse mapToResponse(Submission s) {
        return SubmissionResponse.builder()
                .id(s.getId())
                .problemId(s.getProblem().getId())
                .problemTitle(s.getProblem().getTitle())
                .problemSlug(s.getProblem().getSlug())
                .userId(s.getUser().getId())
                .username(s.getUser().getUsername())
                .language(s.getLanguage())
                .sourceCode(s.getSourceCode())
                .status(s.getStatus())
                .verdict(s.getVerdict())
                .executionTimeMs(s.getExecutionTimeMs())
                .memoryUsedKb(s.getMemoryUsedKb())
                .errorOutput(s.getErrorOutput())
                .submittedAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
