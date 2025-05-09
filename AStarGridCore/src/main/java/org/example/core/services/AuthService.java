package org.example.core.services;

import org.example.core.clients.ServerClient;
import org.example.core.models.dto.LoginResponse;
import org.example.core.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthService {
    private final ServerClient serverClient;
    private final PreferencesStorage preferencesStorage;

    @Autowired
    public AuthService(ServerClient serverClient, PreferencesStorage preferencesStorage) {
        this.serverClient = serverClient;
        this.preferencesStorage = preferencesStorage;
    }

    public ResponseEntity<String> authenticate(String username, String password) {
        Mono<LoginResponse> authResponseMono = serverClient.login(username, password);
        LoginResponse authResponse = authResponseMono.block();

        if (authResponse != null) {
            preferencesStorage.saveTokens(authResponse.getAccessToken(), authResponse.getRefreshToken());
            return ResponseEntity.ok(authResponse.getAccessToken());
        } else {
            return ResponseEntity.status(401).body("Authentication failed");
        }
    }

    public User getUser() {
        var t = serverClient.getUser();
        return  t.block();
    }
}