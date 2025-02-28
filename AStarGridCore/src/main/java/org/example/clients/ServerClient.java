package org.example.clients;

import org.example.configurations.AppSettings;
import org.example.models.*;
import org.example.models.dto.*;
import org.example.services.PreferencesStorage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class ServerClient extends ServerClientBase {
    public ServerClient(WebClient.Builder webClientBuilder,
                        AppSettings appSettings,
                        PreferencesStorage preferencesStorage) {
        super(appSettings, webClientBuilder, preferencesStorage);
    }

    public Mono<LoginResponse> login(String username, String password) {
        return webClient.post()
                .uri("/login")
                .bodyValue(new LoginRequest(username, password))
                .retrieve()
                .bodyToMono(LoginResponse.class)
                .doOnNext(response -> preferencesStorage.saveTokens(response.getAccessToken(), response.getRefreshToken())); // Сохраняем токены
    }

    public Mono<User> getUser() {
        return getWithRetry("/user", User.class);
    }

    public Mono<String> getProjects(int page, int perPage) {
        return getWithRetry("/projects?page=" + page + "&per_page=" + perPage, String.class);
    }

    public Mono<String> subscribeToProject(SubscribeTransport subscribeTransport) {
        return postWithRetry("/subscribe", String.class, SubscribeTransport.class, subscribeTransport);
    }

    public Mono<List<SubscribeResponse>> getSubscribes(UUID deviceUUID) {
        return getWithRetry("/subscriptions?device_uuid=" + deviceUUID.toString(), SubscribeResponse[].class)
                .map(List::of);
    }

    public Mono<String> unsubscribeFromProject(int projectId, UUID deviceUUID) {
        return postWithRetry(
                "/unsubscribe?project_id=" + projectId
                        + "&device_uuid=" + deviceUUID, String.class);
    }

    public Mono<Void> downloadTaskArchive(UUID taskUUID, String saveDirectory, String fileName) {
        return getFileWithRetry(
                "/download?task_uuid=" + taskUUID,
                saveDirectory,
                fileName);
    }

    public Mono<CurrentTaskResponse> getCurrentTask(int projectId, UUID deviceUUID) {
        return getWithRetry(
                "/get_current_task?project_id=" + projectId +
                "&device_uuid=" + deviceUUID,
                CurrentTaskResponse.class
        );
    }
}


