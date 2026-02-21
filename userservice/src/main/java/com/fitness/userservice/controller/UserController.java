package com.fitness.userservice.controller;

import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

    private UserService userService;

    @GetMapping("/{userID}")
    public ResponseEntity<UserResponse> getUserProfile(@PathVariable String userID) {
        return ResponseEntity.ok(userService.getUserProfile(userID));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.registerUser(request));
    }

    @GetMapping("/{userID}/validate")
    public ResponseEntity<Boolean> validateUser(@PathVariable String userID) {
        return ResponseEntity.ok(userService.validateUser(userID));
    }
}
