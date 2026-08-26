package com.codeforge.module.submission.dto.response;

import com.codeforge.common.enums.ProgrammingLanguage;
import com.codeforge.common.enums.SubmissionStatus;
import com.codeforge.common.enums.SubmissionVerdict;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResponse {

    private Long id;
    private Long problemId;
    private String problemTitle;
    private String problemSlug;
    private Long userId;
    private String username;
    private ProgrammingLanguage language;
    private String sourceCode;
    private SubmissionStatus status;
    private SubmissionVerdict verdict;
    private Integer executionTimeMs;
    private Integer memoryUsedKb;
    private String errorOutput;
    private Instant submittedAt;
    private Instant updatedAt;
}
