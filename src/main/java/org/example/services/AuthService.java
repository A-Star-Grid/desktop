package org.example.services;

import org.example.clients.ServerClient;
import org.example.models.LoginResponse;
import org.example.models.User;
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

    /**
     * Авторизация пользователя и сохранение токенов.
     */
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

    /**
     * Получение accessToken, обновление при необходимости.
     */
    public String getAccessToken() {
        String accessToken = preferencesStorage.loadAccessToken();
        if (accessToken != null) {
            return accessToken;
        }

        // Если accessToken истек, пробуем обновить через refreshToken
        String refreshToken = preferencesStorage.loadRefreshToken();
        if (refreshToken != null) {
            return refreshAccessToken(refreshToken);
        }

        return null; // Нужна повторная авторизация
    }

    /**
     * Обновление accessToken через refreshToken.
     */
    private String refreshAccessToken(String refreshToken) {
        var refreshResponse = serverClient.refresh(refreshToken).block();

        preferencesStorage.saveTokens(refreshResponse.getAccessToken(), refreshResponse.getRefreshToken());

        return refreshResponse.getAccessToken();
    }

    public User getUser() {
        var jwt = preferencesStorage.loadAccessToken();
        var t = serverClient.getUser(jwt);
        return  t.block();
    }
}