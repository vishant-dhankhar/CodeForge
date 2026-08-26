package com.codeforge.module.submission.service;

import com.codeforge.module.judge.service.SubmissionConsumer;
import com.codeforge.module.submission.dto.queue.SubmissionTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionQueueService {

    public static final String SUBMISSION_QUEUE_KEY = "queue:submissions";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectProvider<SubmissionConsumer> consumerProvider;

    public void pushTask(SubmissionTask task) {
        log.info("Dispatching submission task #{} for problem #{} [{}]",
                task.getSubmissionId(), task.getProblemId(), task.getLanguage());

        try {
            redisTemplate.opsForList().leftPush(SUBMISSION_QUEUE_KEY, task);
            log.info("Pushed submission #{} to Redis queue", task.getSubmissionId());
        } catch (Exception ex) {
            log.warn("Redis queue is offline ({}). Gracefully executing submission #{} via local asynchronous worker.",
                    ex.getMessage(), task.getSubmissionId());

            SubmissionConsumer consumer = consumerProvider.getIfAvailable();
            if (consumer != null) {
                CompletableFuture.runAsync(() -> {
                    try {
                        consumer.processSubmission(task);
                    } catch (Exception err) {
                        log.error("Local submission processing error for #{}", task.getSubmissionId(), err);
                    }
                });
            }
        }
    }
}
