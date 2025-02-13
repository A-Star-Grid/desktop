package org.example.controllers;

import org.example.models.User;
import org.example.models.UserResponse;
import org.example.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password) {
        return authService.authenticate(username, password);
    }

    @GetMapping("/token")
    public ResponseEntity<String> getAccessToken() {
        String token = authService.getAccessToken();
        if (token != null) {
            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.status(401).body("Token not available, please login again.");
        }
    }

    @GetMapping("/getCurrentUser")
    public ResponseEntity<UserResponse> getCurrentToken() {
        return ResponseEntity.ok(new UserResponse(authService.getUser()));
    }
}
