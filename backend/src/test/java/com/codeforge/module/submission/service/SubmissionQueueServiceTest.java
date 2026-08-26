package com.codeforge.module.submission.service;

import com.codeforge.common.enums.ProgrammingLanguage;
import com.codeforge.module.submission.dto.queue.SubmissionTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionQueueServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ListOperations<String, Object> listOperations;

    @InjectMocks
    private SubmissionQueueService queueService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    @Test
    @DisplayName("Should push submission task to Redis queue list")
    void shouldPushTaskToQueue() {
        SubmissionTask task = SubmissionTask.builder()
                .submissionId(101L)
                .problemId(1L)
                .language(ProgrammingLanguage.JAVA)
                .sourceCode("public class Solution {}")
                .timeLimitMs(1000)
                .memoryLimitMb(256)
                .build();

        queueService.pushTask(task);

        verify(listOperations, times(1)).leftPush("queue:submissions", task);
    }
}
