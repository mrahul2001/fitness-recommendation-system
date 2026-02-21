package com.fitness.aiservice.service;

import com.fitness.aiservice.exception.ActivityNotFoundException;
import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.repository.RecommendationRepository;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final RecommendationRepository recommendationRepository;

    public List<Recommendation> getUserRecommendations(String userID) {
        return recommendationRepository.findByUserID(userID);
    }

    public Recommendation getActivityRecommendation(String activityID) {
        return recommendationRepository.findByActivityID(activityID)
                .orElseThrow(() -> new ActivityNotFoundException("Activity not found with ID::: " + activityID));
    }
}
