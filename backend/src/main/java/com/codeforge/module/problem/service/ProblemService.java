package com.codeforge.module.problem.service;

import com.codeforge.common.dto.PagedResponse;
import com.codeforge.common.enums.ProblemDifficulty;
import com.codeforge.module.problem.dto.response.ProblemDetailResponse;
import com.codeforge.module.problem.dto.response.ProblemSummaryResponse;
import org.springframework.data.domain.Pageable;

public interface ProblemService {

    PagedResponse<ProblemSummaryResponse> getProblems(ProblemDifficulty difficulty, String search, Pageable pageable);

    ProblemDetailResponse getProblemBySlug(String slug);
}
