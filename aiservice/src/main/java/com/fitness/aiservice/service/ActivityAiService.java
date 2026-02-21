package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityAiService {
    private final GeminiService geminiService;
    private final RecommendationRepository recommendationRepository;

    public void generateRecommendation(Activity activity) {
        String prompt = createPromptForActivity(activity);
        String aiResponse = geminiService.geminiResponse(prompt);
        processAiResponse(activity, aiResponse);
    }

    private void processAiResponse(Activity activity, String aiResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Extract JSON block between first { and last }
            int start = aiResponse.indexOf("{");
            int end = aiResponse.lastIndexOf("}");

            if (start == -1 || end == -1) {
                throw new RuntimeException("No JSON object found in AI response");
            }

            String jsonOnly = aiResponse.substring(start, end + 1);

            log.info("EXTRACTED JSON >>>{}<<<", jsonOnly);

            JsonNode rootNode = mapper.readTree(jsonOnly);

            log.info("Parsed successfully!");

            // 🔹 Extract fields
            String overallAnalysis =
                    rootNode.path("analysis").path("overall").asText();

            List<String> improvements = new ArrayList<>();
            rootNode.path("improvements").forEach(node ->
                    improvements.add(node.path("area").asText() + ": " + node.path("recommendation").asText())
            );

            List<String> suggestions = new ArrayList<>();
            rootNode.path("suggestions").forEach(node ->
                    suggestions.add(node.path("workout").asText() + ": " + node.path("description").asText())
            );

            List<String> safety = new ArrayList<>();
            rootNode.path("safety").forEach(node ->
                    safety.add(node.asText())
            );

            // 🔹 Combine Activity + AI Data
            Recommendation recommendation = Recommendation.builder()
                    .activityID(activity.getID())
                    .userID(activity.getUserID())
                    .activityType(String.valueOf(activity.getActivityType()))
                    .recommendation(overallAnalysis)
                    .improvements(improvements)
                    .suggestions(suggestions)
                    .safety(safety)
                    .build();

            recommendationRepository.save(recommendation);

        } catch (Exception e) {
            log.error("Failed to parse AI response", e);
        }
    }

    private String createPromptForActivity(Activity activity) {

        return String.format("""
        A user completed this workout:

        Activity: %s
        Duration: %d minutes
        Calories burned: %d
        Pace: %s
        Distance: %s miles
        Heart rate: %s bpm
        Steps: %s

        Recommend next physical activities for this user, and give me the output in the following format:
        
        {
            "analysis": {
                "overall": "overall analysis here",
                "pace": "pace analysis here",
                "heartRate": "heartRate analysis here",
                "caloriesBurnt": "caloriesBurnt analysis here"
            },
            "improvements": [
                {
                    "area": "area name",
                    "recommendation": "detailed recommendation"
                }
            ],
            "suggestions": [
                "workout": "workout name",
                "description": "detailed workout description"
            ],
            "safety": [
                "safety point 1",
                "safety point 2",
                "safety point 3"
            ]
        }
        """,
                activity.getActivityType(),
                activity.getDuration(),
                activity.getCaloriesBurnt(),
                activity.getAdditionalMetrics().get("pace"),
                activity.getAdditionalMetrics().get("distance"),
                activity.getAdditionalMetrics().get("heartRate"),
                activity.getAdditionalMetrics().get("steps")
        );
    }
}
