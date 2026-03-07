package com.fitness.aiservice.controller;

import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {
    private final RecommendationService recommendationService;

    @GetMapping("/user/{userID}")
    public ResponseEntity<List<Recommendation>> getUserRecommendations(@PathVariable String userID) {
        return ResponseEntity.ok(recommendationService.getUserRecommendations(userID));
    }

    @GetMapping("/activity/{activityID}")
    public ResponseEntity<Object> getActivityRecommendation(@PathVariable String activityID) {
        return ResponseEntity.ok(recommendationService.getActivityRecommendation(activityID));
    }
}
