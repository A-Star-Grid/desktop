package org.example.core.clients;

import org.example.core.configurations.AppSettings;
import org.example.core.models.*;
import org.example.core.models.dto.*;
import org.example.core.services.PreferencesStorage;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
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

    public Mono<ProjectsResponse> getProjects(int page, int perPage) {
        return getWithRetry("/projects_paginate?page=" + page + "&per_page=" + perPage, ProjectsResponse.class);
    }

    public Mono<ProjectsResponse> getProjects() {
        return getWithRetry("/projects", ProjectsResponse.class);
    }

    public Mono<String> getStatistic() {
        return getWithRetry("/user_statistics", String.class);
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

    public Mono<String> cancelProjectTask(int projectId, UUID deviceUUID, UUID taskUuid) {
        return postWithRetry(
                "/cancel_task?project_id=" + projectId
                        + "&device_uuid=" + deviceUUID
                        + "&task_uuid=" + taskUuid, String.class);
    }

    public Mono<String> getCancelledTasks() {
        return getWithRetry(
                "/cancelled_tasks", String.class);
    }

    public Mono<Void> downloadTaskArchive(UUID taskUUID, String saveDirectory, String fileName) {
        return getFileWithRetry(
                "/download?task_uuid=" + taskUUID,
                saveDirectory,
                fileName);
    }

    public Mono<UploadResult> uploadResultArchive(UUID taskUUID, int projectId, UUID deviceUUID, String filePath) {
        var file = new File(filePath);

        if (!file.exists() || !file.isFile()) {
            return Mono.error(new RuntimeException("Файл не найден: " + filePath));
        }

        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", new FileSystemResource(file))
                .contentType(MediaType.APPLICATION_OCTET_STREAM);

        return postFileWithRetry("/upload_result?task_uuid=" + taskUUID.toString() +
                        "&project_id=" + projectId +
                        "&device_uuid=" + deviceUUID,
                bodyBuilder)
                .doOnSuccess(response -> System.out.println("📤 Файл успешно загружен: " + filePath))
                .doOnError(error -> System.err.println("⚠ Ошибка загрузки файла: " + error.getMessage()));
    }

    public Mono<CurrentTaskResponse> getCurrentTask(int projectId, UUID deviceUUID) {
        return getWithRetry(
                "/get_current_task?project_id=" + projectId +
                        "&device_uuid=" + deviceUUID,
                CurrentTaskResponse.class
        );
    }
}


