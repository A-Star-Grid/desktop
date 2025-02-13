package org.example.clients;

import org.example.configurations.AppSettings;
import org.example.models.*;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ServerClient {

    private final WebClient webClient;
    private final AppSettings appSettings;

    public ServerClient(WebClient.Builder webClientBuilder, AppSettings appSettings) {
        this.appSettings = appSettings;
        this.webClient = webClientBuilder.baseUrl(this.appSettings.serverUrl).build();
    }

    public Mono<LoginResponse> login(String username, String password) {
        return webClient.post()
                .uri("/login")
                .bodyValue(new LoginRequest(username, password))
                .retrieve()
                .bodyToMono(LoginResponse.class);
    }

    public Mono<RefreshResponse> refresh(String refreshToken) {
        return webClient.post()
                .uri("/refresh")
                .bodyValue(new RefreshRequest(refreshToken))
                .retrieve()
                .bodyToMono(RefreshResponse.class);
    }


    public Mono<User> getUser(String jwt) {
        return webClient.get()
                .uri("/user")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .retrieve()
                .bodyToMono(User.class);
    }

    public Mono<String> getProjects(String token) {
        return webClient.get()
                .uri("/projects?page=1&per_page=5")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> getProjects(String token, int page, int perPage) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/projects")
                        .queryParam("page", page)
                        .queryParam("per_page", perPage)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class);
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


