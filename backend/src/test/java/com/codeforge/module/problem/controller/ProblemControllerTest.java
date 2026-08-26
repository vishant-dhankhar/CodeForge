package com.codeforge.module.problem.controller;

import com.codeforge.common.dto.PagedResponse;
import com.codeforge.common.enums.ProblemDifficulty;
import com.codeforge.common.exception.GlobalExceptionHandler;
import com.codeforge.common.exception.ResourceNotFoundException;
import com.codeforge.module.problem.dto.response.ProblemDetailResponse;
import com.codeforge.module.problem.dto.response.ProblemSummaryResponse;
import com.codeforge.module.problem.dto.response.TestCaseResponse;
import com.codeforge.module.problem.service.ProblemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProblemControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProblemService problemService;

    @InjectMocks
    private ProblemController problemController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(problemController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/problems should return 200 OK with paginated list")
    void shouldReturnPaginatedProblems() throws Exception {
        ProblemSummaryResponse item = ProblemSummaryResponse.builder()
                .id(1L)
                .slug("two-sum")
                .title("Two Sum")
                .difficulty(ProblemDifficulty.EASY)
                .timeLimitMs(1000)
                .memoryLimitMb(256)
                .tags(List.of("Array", "Hash Table"))
                .build();

        PagedResponse<ProblemSummaryResponse> pagedResponse = PagedResponse.<ProblemSummaryResponse>builder()
                .content(List.of(item))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .isFirst(true)
                .isLast(true)
                .build();

        when(problemService.getProblems(eq(ProblemDifficulty.EASY), eq("two"), any(Pageable.class)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/problems")
                        .param("difficulty", "EASY")
                        .param("search", "two")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].slug").value("two-sum"))
                .andExpect(jsonPath("$.data.content[0].title").value("Two Sum"))
                .andExpect(jsonPath("$.data.content[0].difficulty").value("EASY"));
    }

    @Test
    @DisplayName("GET /api/v1/problems/{slug} should return 200 OK with problem details")
    void shouldReturnProblemDetail() throws Exception {
        TestCaseResponse sampleTestCase = TestCaseResponse.builder()
                .id(10L)
                .inputData("4\n2 7 11 15\n9")
                .expectedOutput("0 1")
                .explanation("2 + 7 = 9")
                .orderIndex(1)
                .build();

        ProblemDetailResponse detailResponse = ProblemDetailResponse.builder()
                .id(1L)
                .slug("two-sum")
                .title("Two Sum")
                .description("Find two numbers that add up to target...")
                .difficulty(ProblemDifficulty.EASY)
                .timeLimitMs(1000)
                .memoryLimitMb(256)
                .tags(List.of("Array"))
                .sampleTestCases(List.of(sampleTestCase))
                .build();

        when(problemService.getProblemBySlug("two-sum"))
                .thenReturn(detailResponse);

        mockMvc.perform(get("/api/v1/problems/two-sum")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.slug").value("two-sum"))
                .andExpect(jsonPath("$.data.sampleTestCases[0].expectedOutput").value("0 1"));
    }

    @Test
    @DisplayName("GET /api/v1/problems/{slug} should return 404 when slug does not exist")
    void shouldReturn404WhenProblemNotFound() throws Exception {
        when(problemService.getProblemBySlug("unknown-problem"))
                .thenThrow(new ResourceNotFoundException("Problem", "slug", "unknown-problem"));

        mockMvc.perform(get("/api/v1/problems/unknown-problem")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }
}
