package com.codeforge.module.judge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResult {

    private int exitCode;
    private String stdout;
    private String stderr;
    private int executionTimeMs;
    private int memoryUsedKb;
    private boolean isTimeout;
    private boolean isCompileError;
}
