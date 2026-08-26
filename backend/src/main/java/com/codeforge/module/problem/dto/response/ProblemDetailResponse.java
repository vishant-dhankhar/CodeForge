package com.codeforge.module.problem.dto.response;

import com.codeforge.common.enums.ProblemDifficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemDetailResponse {

    private Long id;
    private String slug;
    private String title;
    private String description;
    private ProblemDifficulty difficulty;
    private int timeLimitMs;
    private int memoryLimitMb;
    private List<String> tags;
    private List<TestCaseResponse> sampleTestCases;
}
