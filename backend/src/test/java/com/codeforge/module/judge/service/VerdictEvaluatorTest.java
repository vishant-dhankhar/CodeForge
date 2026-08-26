package com.codeforge.module.judge.service;

import com.codeforge.common.enums.SubmissionVerdict;
import com.codeforge.module.judge.dto.ExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VerdictEvaluatorTest {

    private VerdictEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new VerdictEvaluator();
    }

    @Test
    @DisplayName("Should evaluate as ACCEPTED when output matches expected output exactly")
    void shouldEvaluateAccepted() {
        ExecutionResult result = ExecutionResult.builder()
                .exitCode(0)
                .stdout("0 1\n")
                .build();

        SubmissionVerdict verdict = evaluator.evaluate(result, "0 1");
        assertEquals(SubmissionVerdict.ACCEPTED, verdict);
    }

    @Test
    @DisplayName("Should evaluate as ACCEPTED when output matches after whitespace and newline normalization")
    void shouldEvaluateAcceptedWithWhitespaceDifferences() {
        ExecutionResult result = ExecutionResult.builder()
                .exitCode(0)
                .stdout("  0 1 \r\n\r\n")
                .build();

        SubmissionVerdict verdict = evaluator.evaluate(result, "0 1\n");
        assertEquals(SubmissionVerdict.ACCEPTED, verdict);
    }

    @Test
    @DisplayName("Should evaluate as WRONG_ANSWER when output does not match")
    void shouldEvaluateWrongAnswer() {
        ExecutionResult result = ExecutionResult.builder()
                .exitCode(0)
                .stdout("1 2")
                .build();

        SubmissionVerdict verdict = evaluator.evaluate(result, "0 1");
        assertEquals(SubmissionVerdict.WRONG_ANSWER, verdict);
    }

    @Test
    @DisplayName("Should evaluate as TIME_LIMIT_EXCEEDED when isTimeout is true")
    void shouldEvaluateTimeLimitExceeded() {
        ExecutionResult result = ExecutionResult.builder()
                .isTimeout(true)
                .build();

        SubmissionVerdict verdict = evaluator.evaluate(result, "0 1");
        assertEquals(SubmissionVerdict.TIME_LIMIT_EXCEEDED, verdict);
    }

    @Test
    @DisplayName("Should evaluate as COMPILATION_ERROR when isCompileError is true")
    void shouldEvaluateCompilationError() {
        ExecutionResult result = ExecutionResult.builder()
                .isCompileError(true)
                .stderr("error: ';' expected")
                .build();

        SubmissionVerdict verdict = evaluator.evaluate(result, "0 1");
        assertEquals(SubmissionVerdict.COMPILATION_ERROR, verdict);
    }

    @Test
    @DisplayName("Should evaluate as RUNTIME_ERROR when exit code is non-zero")
    void shouldEvaluateRuntimeError() {
        ExecutionResult result = ExecutionResult.builder()
                .exitCode(1)
                .stderr("Exception in thread \"main\" java.lang.ArrayIndexOutOfBoundsException")
                .build();

        SubmissionVerdict verdict = evaluator.evaluate(result, "0 1");
        assertEquals(SubmissionVerdict.RUNTIME_ERROR, verdict);
    }
}
