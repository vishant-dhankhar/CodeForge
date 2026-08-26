package com.codeforge.module.problem.controller;

import com.codeforge.common.dto.ApiResponse;
import com.codeforge.common.dto.PagedResponse;
import com.codeforge.common.enums.ProblemDifficulty;
import com.codeforge.module.problem.dto.response.ProblemDetailResponse;
import com.codeforge.module.problem.dto.response.ProblemSummaryResponse;
import com.codeforge.module.problem.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ProblemSummaryResponse>>> getProblems(
            @RequestParam(required = false) ProblemDifficulty difficulty,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50), Sort.by("id").ascending());
        PagedResponse<ProblemSummaryResponse> response = problemService.getProblems(difficulty, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Problems fetched successfully"));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<ProblemDetailResponse>> getProblemBySlug(
            @PathVariable String slug
    ) {
        ProblemDetailResponse response = problemService.getProblemBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(response, "Problem fetched successfully"));
    }
}
