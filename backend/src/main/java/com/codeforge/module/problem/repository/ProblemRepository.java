package com.codeforge.module.problem.repository;

import com.codeforge.common.enums.ProblemDifficulty;
import com.codeforge.module.problem.entity.Problem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {

    Optional<Problem> findBySlugAndIsPublishedTrue(String slug);

    @Query("SELECT p FROM Problem p WHERE p.isPublished = true " +
           "AND (:difficulty IS NULL OR p.difficulty = :difficulty) " +
           "AND (:search IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Problem> findPublishedProblems(
            @Param("difficulty") ProblemDifficulty difficulty,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT DISTINCT p FROM Problem p LEFT JOIN FETCH p.testCases WHERE p.id = :id")
    Optional<Problem> findByIdWithTestCases(@Param("id") Long id);
}
