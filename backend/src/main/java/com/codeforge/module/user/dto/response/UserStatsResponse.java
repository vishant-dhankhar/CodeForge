package com.codeforge.module.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {

    private int easySolved;
    private int mediumSolved;
    private int hardSolved;
    private int totalSolved;
    private int totalSubmissions;
    private int acceptedSubmissions;
    private double acceptanceRate;
}
