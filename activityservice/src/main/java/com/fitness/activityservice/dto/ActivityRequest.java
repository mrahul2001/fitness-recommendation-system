package com.fitness.activityservice.dto;

import com.fitness.activityservice.model.ActivityType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ActivityRequest {
    private String userID;
    private ActivityType activityType;
    private Integer duration;
    private Integer caloriesBurnt;
    private LocalDateTime startTime;
    private Map<String, Object> additionalMetrics;
}
