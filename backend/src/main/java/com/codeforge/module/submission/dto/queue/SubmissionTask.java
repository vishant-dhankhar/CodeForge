package com.codeforge.module.submission.dto.queue;

import com.codeforge.common.enums.ProgrammingLanguage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionTask implements Serializable {

    private Long submissionId;
    private Long problemId;
    private ProgrammingLanguage language;
    private String sourceCode;
    private int timeLimitMs;
    private int memoryLimitMb;
}
