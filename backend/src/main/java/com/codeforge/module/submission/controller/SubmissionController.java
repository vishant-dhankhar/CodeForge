package com.codeforge.module.submission.controller;

import com.codeforge.common.dto.ApiResponse;
import com.codeforge.common.dto.PagedResponse;
import com.codeforge.common.ratelimit.RateLimit;
import com.codeforge.module.submission.dto.request.CreateSubmissionRequest;
import com.codeforge.module.submission.dto.response.SubmissionResponse;
import com.codeforge.module.submission.service.SubmissionService;
import com.codeforge.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    @RateLimit(limit = 10, windowSeconds = 60, keyPrefix = "create_submission")
    public ResponseEntity<ApiResponse<SubmissionResponse>> createSubmission(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateSubmissionRequest request
    ) {
        SubmissionResponse response = submissionService.createSubmission(currentUser, request);
        return new ResponseEntity<>(
                ApiResponse.success(response, "Submission queued for evaluation"),
                HttpStatus.ACCEPTED
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubmissionResponse>> getSubmissionById(@PathVariable Long id) {
        SubmissionResponse response = submissionService.getSubmissionById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Submission status fetched successfully"));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PagedResponse<SubmissionResponse>>> getMySubmissions(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) Long problemId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50), Sort.by("id").descending());
        PagedResponse<SubmissionResponse> response = submissionService.getMySubmissions(currentUser, problemId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "User submissions fetched successfully"));
    }

    @GetMapping("/problem/{slug}")
    public ResponseEntity<ApiResponse<PagedResponse<SubmissionResponse>>> getSubmissionsByProblem(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50), Sort.by("id").descending());
        PagedResponse<SubmissionResponse> response = submissionService.getSubmissionsByProblemSlug(slug, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Problem submissions fetched successfully"));
    }
}
