package com.fitness.aiservice.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class Activity {
    private String ID;
    private String userID;
    private ActivityType activityType;
    private Integer duration;
    private Integer caloriesBurnt;
    private LocalDateTime startTime;
    private Map<String, Object> additionalMetrics;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
