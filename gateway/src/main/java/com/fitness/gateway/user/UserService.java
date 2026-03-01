package com.fitness.gateway.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final WebClient userWebClient;

    public Mono<Boolean> validateUser(String userID) throws Exception {
        log.info("Calling User Validation for UserID: {}", userID);
        return userWebClient
                .get()
                .uri("/api/users/{userID}/validate", userID)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    if (e.getStatusCode() == HttpStatus.NOT_FOUND)
                        return Mono.error(new RuntimeException("User Not Found: " + userID));
                    else if (e.getStatusCode() == HttpStatus.BAD_REQUEST)
                        return Mono.error(new RuntimeException("Invlaid Request: " + userID));
                    else
                        return Mono.error(new RuntimeException("Something went wrong"));
                });
    }

    public Mono<UserResponse> registerUser(RegisterRequest request) {

        log.info("Calling User Registration for Email: {}", request.getEmail());

        return userWebClient
                .post()
                .uri("/api/users/register")
                .bodyValue(request)
                .retrieve()

                .onStatus(HttpStatusCode::isError, response ->
                        response.createException()
                                .map(ex -> {
                                    log.error("User Service Status Code: {}", ex.getStatusCode());
                                    log.error("User Service Error Body: {}", ex.getResponseBodyAsString());
                                    return ex;   // ✔ returns Mono<WebClientResponseException>
                                })
                )

                .bodyToMono(UserResponse.class)
                .doOnError(err ->
                        log.error("Registration Failed: {}", err.getMessage()));
    }
}
