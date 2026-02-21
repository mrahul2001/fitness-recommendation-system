package com.fitness.activityservice.service;

import com.fitness.activityservice.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserValidationService {
    private final WebClient userWebClient;

    public boolean validateUser(String userID) {
        log.info("Calling User Validation for UserID: {}", userID);
        try {
            return Boolean.TRUE.equals(userWebClient
                    .get()
                    .uri("/api/users/{userID}/validate", userID)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block());
        } catch (WebClientResponseException e) {
            if(e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new UserNotFoundException("Unable to find User ID::: " + userID);
            }

            return false;
        }
    }
}
