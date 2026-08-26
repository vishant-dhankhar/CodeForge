package com.codeforge.module.submission.service;

import com.codeforge.common.dto.PagedResponse;
import com.codeforge.module.submission.dto.request.CreateSubmissionRequest;
import com.codeforge.module.submission.dto.response.SubmissionResponse;
import com.codeforge.security.UserPrincipal;
import org.springframework.data.domain.Pageable;

public interface SubmissionService {

    SubmissionResponse createSubmission(UserPrincipal currentUser, CreateSubmissionRequest request);

    SubmissionResponse getSubmissionById(Long id);

    PagedResponse<SubmissionResponse> getMySubmissions(UserPrincipal currentUser, Long problemId, Pageable pageable);

    PagedResponse<SubmissionResponse> getSubmissionsByProblemSlug(String problemSlug, Pageable pageable);
}
