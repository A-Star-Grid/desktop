package org.example.clients;

import org.example.configurations.AppSettings;
import org.example.models.*;
import org.example.services.PreferencesStorage;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class ServerClient {


    private final WebClient webClient;
    private final AppSettings appSettings;
    private final PreferencesStorage preferencesStorage;

    public ServerClient(WebClient.Builder webClientBuilder,
                        AppSettings appSettings,
                        PreferencesStorage preferencesStorage) {
        this.appSettings = appSettings;
        this.preferencesStorage = preferencesStorage;
        this.webClient = webClientBuilder.baseUrl(this.appSettings.serverUrl).build();
    }

    public Mono<LoginResponse> login(String username, String password) {
        return webClient.post()
                .uri("/login")
                .bodyValue(new LoginRequest(username, password))
                .retrieve()
                .bodyToMono(LoginResponse.class)
                .doOnNext(response -> preferencesStorage.saveTokens(response.getAccessToken(), response.getRefreshToken())); // Сохраняем токены
    }

    public Mono<RefreshResponse> refresh(String refreshToken) {
        return webClient.post()
                .uri("/refresh")
                .bodyValue(new RefreshRequest(refreshToken))
                .retrieve()
                .bodyToMono(RefreshResponse.class)
                .doOnNext(response -> preferencesStorage.saveTokens(response.getAccessToken(), response.getRefreshToken())); // Обновляем токены
    }

    public Mono<User> getUser() {
        return getWithRetry("/user", User.class);
    }

    public Mono<String> getProjects(int page, int perPage) {
        return getWithRetry("/projects?page=" + page + "&per_page=" + perPage, String.class);
    }

    private <T> Mono<T> getWithRetry(String uri, Class<T> responseType) {
        return sendRequestWithRetry(webClient.get().uri(uri), responseType);
    }

    private <T> Mono<T> sendRequestWithRetry(WebClient.RequestHeadersSpec<?> request, Class<T> responseType) {
        return request
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + preferencesStorage.loadAccessToken()) // Добавляем токен
                .retrieve()
                .onStatus(httpStatus -> httpStatus.value() == 401, response -> handleUnauthorized()) // Если 401 – обновляем токен
                .bodyToMono(responseType)
                .retryWhen(Retry.backoff(1, Duration.ofMillis(500))); // Пробуем еще раз
    }

    private Mono<? extends Throwable> handleUnauthorized() {
        String refreshToken = preferencesStorage.loadRefreshToken();
        if (refreshToken == null) {
            return Mono.error(new RuntimeException("Unauthorized and no refresh token available"));
        }
        return refresh(refreshToken).flatMap(response -> {
            preferencesStorage.saveTokens(response.getAccessToken(), response.getRefreshToken());
            return Mono.empty();
        });
    }

    public Mono<String> subscribeToProject(String token, int projectId) {
        return webClient.post()
                .uri("/subscribe/" + projectId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> unsubscribeFromProject(String token, int projectId) {
        return webClient.post()
                .uri("/unsubscribe/" + projectId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> getUserSubscriptions(String token) {
        return webClient.get()
                .uri("/subscriptions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> getTasks(String token, int projectId) {
        return webClient.get()
                .uri("/tasks/" + projectId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class);
    }
}


