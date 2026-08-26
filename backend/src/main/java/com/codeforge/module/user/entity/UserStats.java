package com.codeforge.module.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "user_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserStats {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "easy_solved", nullable = false)
    @Builder.Default
    private int easySolved = 0;

    @Column(name = "medium_solved", nullable = false)
    @Builder.Default
    private int mediumSolved = 0;

    @Column(name = "hard_solved", nullable = false)
    @Builder.Default
    private int hardSolved = 0;

    @Column(name = "total_submissions", nullable = false)
    @Builder.Default
    private int totalSubmissions = 0;

    @Column(name = "accepted_submissions", nullable = false)
    @Builder.Default
    private int acceptedSubmissions = 0;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    public int getTotalSolved() {
        return easySolved + mediumSolved + hardSolved;
    }

    public double getAcceptanceRate() {
        if (totalSubmissions == 0) {
            return 0.0;
        }
        return Math.round(((double) acceptedSubmissions / totalSubmissions) * 10000.0) / 100.0;
    }
}
