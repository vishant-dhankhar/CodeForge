package com.codeforge.module.user.repository;

import com.codeforge.module.user.entity.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStatsRepository extends JpaRepository<UserStats, Long> {
}
