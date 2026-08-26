package com.codeforge.module.problem.entity;

import com.codeforge.common.entity.BaseEntity;
import com.codeforge.common.enums.ProblemDifficulty;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "problems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Problem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProblemDifficulty difficulty;

    @Column(name = "time_limit_ms", nullable = false)
    @Builder.Default
    private int timeLimitMs = 1000;

    @Column(name = "memory_limit_mb", nullable = false)
    @Builder.Default
    private int memoryLimitMb = 256;

    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private boolean isPublished = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "problem_tags",
            joinColumns = @JoinColumn(name = "problem_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    @JsonManagedReference
    @Builder.Default
    private List<ProblemTestCase> testCases = new ArrayList<>();
}
