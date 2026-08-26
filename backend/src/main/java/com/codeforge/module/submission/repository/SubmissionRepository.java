package com.codeforge.module.submission.repository;

import com.codeforge.module.submission.entity.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    @Query("SELECT s FROM Submission s JOIN FETCH s.problem JOIN FETCH s.user WHERE s.id = :id")
    Optional<Submission> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT s FROM Submission s JOIN FETCH s.problem JOIN FETCH s.user WHERE s.user.id = :userId")
    Page<Submission> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT s FROM Submission s JOIN FETCH s.problem JOIN FETCH s.user WHERE s.user.id = :userId AND s.problem.id = :problemId")
    Page<Submission> findByUserIdAndProblemId(@Param("userId") Long userId, @Param("problemId") Long problemId, Pageable pageable);

    @Query("SELECT s FROM Submission s JOIN FETCH s.problem JOIN FETCH s.user WHERE s.problem.slug = :slug")
    Page<Submission> findByProblemSlug(@Param("slug") String slug, Pageable pageable);

    boolean existsByUserIdAndProblemIdAndVerdict(Long userId, Long problemId, com.codeforge.common.enums.SubmissionVerdict verdict);

    long countByUserIdAndProblemIdAndVerdict(Long userId, Long problemId, com.codeforge.common.enums.SubmissionVerdict verdict);
}
