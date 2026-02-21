package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {

    private final ActivityAiService activityAiService;

    @RabbitListener(queues = "#{activityQueue.name}")
    public void processActivity(Activity activity) {
        try {
            log.info("Received activity for processing: {}", activity.getID());
            activityAiService.generateRecommendation(activity);
        } catch (Exception e) {
            log.error("Failed to generate recommendation: {}", e.getMessage());
            // DO NOT rethrow → prevents infinite retry
        }
    }
}
