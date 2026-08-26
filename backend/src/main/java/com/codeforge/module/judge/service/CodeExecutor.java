package com.codeforge.module.judge.service;

import com.codeforge.common.enums.ProgrammingLanguage;
import com.codeforge.module.judge.dto.ExecutionResult;

public interface CodeExecutor {

    ExecutionResult execute(
            ProgrammingLanguage language,
            String sourceCode,
            String inputData,
            int timeLimitMs,
            int memoryLimitMb
    );
}
