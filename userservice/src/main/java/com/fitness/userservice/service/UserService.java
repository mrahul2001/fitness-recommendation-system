package com.fitness.userservice.service;

import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.exception.UserNotFoundException;
import com.fitness.userservice.model.User;
import com.fitness.userservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {

    private UserRepository userRepository;

    public UserResponse registerUser(RegisterRequest registerRequest) {

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            User existingUser = userRepository.findByEmail(registerRequest.getEmail());
            return getUserResponse(existingUser);
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword());
        user.setKeycloakId(registerRequest.getKeycloakId());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());

        User savedUser = userRepository.save(user);
        return getUserResponse(savedUser);
    }

    @NonNull
    private UserResponse getUserResponse(User fetchingUser) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(fetchingUser.getId());
        userResponse.setKeycloakId(fetchingUser.getKeycloakId());
        userResponse.setEmail(fetchingUser.getEmail());
        userResponse.setPassword(fetchingUser.getPassword());
        userResponse.setFirstName(fetchingUser.getFirstName());
        userResponse.setLastName(fetchingUser.getLastName());
        userResponse.setCreatedAt(fetchingUser.getCreatedAt());
        userResponse.setUpdatedAt(fetchingUser.getUpdatedAt());

        return userResponse;
    }

    public UserResponse getUserProfile(String userID) {
        User user = userRepository.findById(userID)
                .orElseThrow(()-> new UserNotFoundException("User not found with ID: " + userID));

        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        userResponse.setPassword(user.getPassword());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setCreatedAt(user.getCreatedAt());
        userResponse.setUpdatedAt(user.getUpdatedAt());

        return userResponse;
    }

    public Boolean validateUser(String userID) {
        log.info("Calling User Validation for UserID: {}", userID);
        return userRepository.existsByKeycloakId(userID);
    }
}
