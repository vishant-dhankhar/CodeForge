package com.codeforge.module.submission.dto.request;

import com.codeforge.common.enums.ProgrammingLanguage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubmissionRequest {

    @NotNull(message = "Problem ID is required")
    private Long problemId;

    @NotNull(message = "Programming language is required")
    private ProgrammingLanguage language;

    @NotBlank(message = "Source code cannot be empty")
    @Size(max = 65536, message = "Source code exceeds maximum allowed size (64KB)")
    private String sourceCode;
}
