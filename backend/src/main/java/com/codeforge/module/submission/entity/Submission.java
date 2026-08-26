package com.codeforge.module.submission.entity;

import com.codeforge.common.entity.BaseEntity;
import com.codeforge.common.enums.ProgrammingLanguage;
import com.codeforge.common.enums.SubmissionStatus;
import com.codeforge.common.enums.SubmissionVerdict;
import com.codeforge.module.problem.entity.Problem;
import com.codeforge.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProgrammingLanguage language;

    @Column(name = "source_code", nullable = false, columnDefinition = "TEXT")
    private String sourceCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SubmissionVerdict verdict;

    @Column(name = "execution_time_ms")
    private Integer executionTimeMs;

    @Column(name = "memory_used_kb")
    private Integer memoryUsedKb;

    @Column(name = "error_output", columnDefinition = "TEXT")
    private String errorOutput;
}
