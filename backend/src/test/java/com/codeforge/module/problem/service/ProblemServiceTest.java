package com.codeforge.module.problem.service;

import com.codeforge.common.dto.PagedResponse;
import com.codeforge.common.enums.ProblemDifficulty;
import com.codeforge.common.exception.ResourceNotFoundException;
import com.codeforge.module.problem.dto.response.ProblemDetailResponse;
import com.codeforge.module.problem.dto.response.ProblemSummaryResponse;
import com.codeforge.module.problem.entity.Problem;
import com.codeforge.module.problem.entity.ProblemTestCase;
import com.codeforge.module.problem.entity.Tag;
import com.codeforge.module.problem.repository.ProblemRepository;
import com.codeforge.module.problem.service.impl.ProblemServiceImpl;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProblemServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @InjectMocks
    private ProblemServiceImpl problemService;

    private Problem sampleProblem;

    @BeforeEach
    void setUp() {
        Tag arrayTag = Tag.builder().id(1L).name("Array").slug("array").build();
        ProblemTestCase sampleTestCase = ProblemTestCase.builder()
                .id(10L)
                .inputData("4\n2 7 11 15\n9")
                .expectedOutput("0 1")
                .isSample(true)
                .explanation("2 + 7 = 9")
                .orderIndex(1)
                .build();

        ProblemTestCase hiddenTestCase = ProblemTestCase.builder()
                .id(11L)
                .inputData("2\n3 3\n6")
                .expectedOutput("0 1")
                .isSample(false)
                .orderIndex(2)
                .build();

        sampleProblem = Problem.builder()
                .id(1L)
                .slug("two-sum")
                .title("Two Sum")
                .description("Find two indices...")
                .difficulty(ProblemDifficulty.EASY)
                .timeLimitMs(1000)
                .memoryLimitMb(256)
                .isPublished(true)
                .tags(Set.of(arrayTag))
                .testCases(List.of(sampleTestCase, hiddenTestCase))
                .build();
    }

    @Test
    @DisplayName("Should return paginated list of problem summaries")
    void shouldReturnPaginatedProblems() {
        Pageable pageable = PageRequest.of(0, 10);
        when(problemRepository.findPublishedProblems(eq(ProblemDifficulty.EASY), eq("Two"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleProblem), pageable, 1));

        PagedResponse<ProblemSummaryResponse> response =
                problemService.getProblems(ProblemDifficulty.EASY, "Two", pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        ProblemSummaryResponse summary = response.getContent().get(0);
        assertEquals("two-sum", summary.getSlug());
        assertEquals("Two Sum", summary.getTitle());
        assertEquals(ProblemDifficulty.EASY, summary.getDifficulty());
        assertTrue(summary.getTags().contains("Array"));
    }

    @Test
    @DisplayName("Should return problem details with only sample test cases")
    void shouldReturnProblemDetailExcludingHiddenTestCases() {
        when(problemRepository.findBySlugAndIsPublishedTrue("two-sum"))
                .thenReturn(Optional.of(sampleProblem));

        ProblemDetailResponse detail = problemService.getProblemBySlug("two-sum");

        assertNotNull(detail);
        assertEquals("two-sum", detail.getSlug());
        assertEquals("Two Sum", detail.getTitle());
        assertEquals(1, detail.getSampleTestCases().size());
        assertEquals("0 1", detail.getSampleTestCases().get(0).getExpectedOutput());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when problem slug does not exist")
    void shouldThrowExceptionWhenSlugNotFound() {
        when(problemRepository.findBySlugAndIsPublishedTrue("non-existent"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                problemService.getProblemBySlug("non-existent"));
    }
}
