package com.codeforge.module.judge.service;

import com.codeforge.common.enums.SubmissionVerdict;
import com.codeforge.module.judge.dto.ExecutionResult;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class VerdictEvaluator {

    public SubmissionVerdict evaluate(ExecutionResult result, String expectedOutput) {
        if (result.isCompileError()) {
            return SubmissionVerdict.COMPILATION_ERROR;
        }

        if (result.isTimeout()) {
            return SubmissionVerdict.TIME_LIMIT_EXCEEDED;
        }

        if (result.getExitCode() != 0) {
            return SubmissionVerdict.RUNTIME_ERROR;
        }

        if (isOutputMatching(result.getStdout(), expectedOutput)) {
            return SubmissionVerdict.ACCEPTED;
        } else {
            return SubmissionVerdict.WRONG_ANSWER;
        }
    }

    public boolean isOutputMatching(String actual, String expected) {
        if (actual == null && expected == null) return true;
        if (actual == null || expected == null) return false;

        String normalizedActual = normalize(actual);
        String normalizedExpected = normalize(expected);

        return normalizedActual.equals(normalizedExpected);
    }

    public String normalize(String text) {
        if (text == null) return "";
        return Arrays.stream(text.replace("\r\n", "\n").split("\n"))
                .map(String::stripTrailing)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.joining("\n"))
                .trim();
    }
}
