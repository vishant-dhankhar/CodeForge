package com.codeforge.module.problem.service.impl;

import com.codeforge.common.dto.PagedResponse;
import com.codeforge.common.enums.ProblemDifficulty;
import com.codeforge.common.exception.ResourceNotFoundException;
import com.codeforge.module.problem.dto.response.ProblemDetailResponse;
import com.codeforge.module.problem.dto.response.ProblemSummaryResponse;
import com.codeforge.module.problem.dto.response.TestCaseResponse;
import com.codeforge.module.problem.entity.Problem;
import com.codeforge.module.problem.entity.ProblemTestCase;
import com.codeforge.module.problem.entity.Tag;
import com.codeforge.module.problem.repository.ProblemRepository;
import com.codeforge.module.problem.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemServiceImpl implements ProblemService {

    private final ProblemRepository problemRepository;

    @Override
    public PagedResponse<ProblemSummaryResponse> getProblems(
            ProblemDifficulty difficulty, String search, Pageable pageable) {

        Page<Problem> page = problemRepository.findPublishedProblems(difficulty, search, pageable);

        Page<ProblemSummaryResponse> dtoPage = page.map(this::mapToSummary);
        return PagedResponse.fromPage(dtoPage);
    }

    @Override
    @org.springframework.cache.annotation.Cacheable(value = "problems", key = "#slug")
    public ProblemDetailResponse getProblemBySlug(String slug) {
        Problem problem = problemRepository.findBySlugAndIsPublishedTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Problem", "slug", slug));

        return mapToDetail(problem);
    }

    private ProblemSummaryResponse mapToSummary(Problem problem) {
        List<String> tagNames = problem.getTags().stream()
                .map(Tag::getName)
                .sorted()
                .toList();

        return ProblemSummaryResponse.builder()
                .id(problem.getId())
                .slug(problem.getSlug())
                .title(problem.getTitle())
                .difficulty(problem.getDifficulty())
                .timeLimitMs(problem.getTimeLimitMs())
                .memoryLimitMb(problem.getMemoryLimitMb())
                .tags(tagNames)
                .build();
    }

    private ProblemDetailResponse mapToDetail(Problem problem) {
        List<String> tagNames = problem.getTags().stream()
                .map(Tag::getName)
                .sorted()
                .toList();

        List<TestCaseResponse> sampleTestCases = problem.getTestCases().stream()
                .filter(ProblemTestCase::isSample)
                .map(tc -> TestCaseResponse.builder()
                        .id(tc.getId())
                        .inputData(tc.getInputData())
                        .expectedOutput(tc.getExpectedOutput())
                        .explanation(tc.getExplanation())
                        .orderIndex(tc.getOrderIndex())
                        .build())
                .toList();

        return ProblemDetailResponse.builder()
                .id(problem.getId())
                .slug(problem.getSlug())
                .title(problem.getTitle())
                .description(problem.getDescription())
                .difficulty(problem.getDifficulty())
                .timeLimitMs(problem.getTimeLimitMs())
                .memoryLimitMb(problem.getMemoryLimitMb())
                .tags(tagNames)
                .sampleTestCases(sampleTestCases)
                .build();
    }
}
