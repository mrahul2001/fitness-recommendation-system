package com.fitness.activityservice.service;

import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.exception.ActivityNotFoundException;
import com.fitness.activityservice.exception.UserNotFoundException;
import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.repository.ActivityRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserValidationService userValidationService;
    private final RabbitTemplate rabbitTemplate;
    @Value("${rabbitmq.exchange.name}")
    private String exchange;
    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    public ActivityResponse trackActivity(ActivityRequest request) {

        log.info("POST /trackActivity called");
        log.info("Request payload: {}", request);

        try {
            log.info("Validating user with ID={}", request.getUserID());
            boolean isValidUser = userValidationService.validateUser(request.getUserID());
            log.info("User validation result={}", isValidUser);

            Activity activity = Activity.builder()
                    .userID(request.getUserID())
                    .activityType(request.getActivityType())
                    .duration(request.getDuration())
                    .caloriesBurnt(request.getCaloriesBurnt())
                    .startTime(request.getStartTime())
                    .additionalMetrics(request.getAdditionalMetrics())
                    .build();

            log.info("Saving activity to MongoDB");
            Activity savedActivity = activityRepository.save(activity);
            log.info("Activity saved with ID={}", savedActivity.getID());

            log.info("Publishing activity to RabbitMQ → exchange={}, routingKey={}", exchange, routingKey);
            rabbitTemplate.convertAndSend(exchange, routingKey, savedActivity);
            log.info("Message published to RabbitMQ");

            return mapToResponse(savedActivity);

        } catch (Exception e) {
            log.error("ERROR during trackActivity()", e);
            throw e; // let Spring return 500 for now
        }
    }

    private ActivityResponse mapToResponse(Activity activity) {
        ActivityResponse response = new ActivityResponse();

        response.setID(activity.getID());
        response.setUserID(activity.getUserID());
        response.setActivityType(activity.getActivityType());
        response.setDuration(activity.getDuration());
        response.setCaloriesBurnt(activity.getCaloriesBurnt());
        response.setStartTime(activity.getStartTime());
        response.setAdditionalMetrics(activity.getAdditionalMetrics());
        response.setCreatedAt(activity.getCreatedAt());
        response.setUpdatedAt(activity.getUpdatedAt());

        return response;
    }

    public List<ActivityResponse> getUserActivities(String userID) {
        List<Activity> userActivities = activityRepository.findByUserID(userID);

        return userActivities.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ActivityResponse getActivityById(String activityID) {
        return activityRepository.findById(activityID)
                .map(this::mapToResponse)
                .orElseThrow(()-> new ActivityNotFoundException("Activity Not Found with ID::: " + activityID));
    }
}
