package org.example.clients;

import org.example.configurations.AppSettings;
import org.example.models.dto.RefreshRequest;
import org.example.models.dto.RefreshResponse;
import org.example.models.dto.UploadResult;
import org.example.services.PreferencesStorage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;

public class ServerClientBase {
    PreferencesStorage preferencesStorage;
    WebClient webClient;
    AppSettings appSettings;

    public ServerClientBase(AppSettings appSettings,
                            WebClient.Builder webClientBuilder,
                            PreferencesStorage preferencesStorage){
        this.preferencesStorage = preferencesStorage;
        this.appSettings = appSettings;
        this.webClient = webClientBuilder.baseUrl(this.appSettings.serverUrl).build();
    }

    protected <T> Mono<T> getWithRetry(String uri, Class<T> responseType) {
        return sendRequestWithRetry(webClient.get().uri(uri), responseType);
    }

    protected Mono<Void> getFileWithRetry(String uri, String saveDirectory, String fileName) {
        return sendRequestWithRetry(webClient.get().uri(uri), byte[].class)
                .flatMap(fileData -> saveFile(fileData, saveDirectory, fileName));
    }

    protected Mono<UploadResult> postFileWithRetry(String uri, MultipartBodyBuilder bodyBuilder) {
        return sendRequestWithRetry(
                webClient.post()
                        .uri(uri)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(BodyInserters.fromMultipartData(bodyBuilder.build())),
                UploadResult.class
        );
    }

    protected <T> Mono<T> postWithRetry(String uri, Class<T> responseType) {
        return sendRequestWithRetry(webClient.post().uri(uri), responseType);
    }

    protected <T, U> Mono<T> postWithRetry(String uri, Class<T> responseType, Class<U> requestType, U requestBody) {
        return sendRequestWithRetry(webClient
                        .post()
                        .uri(uri)
                        .body(Mono.just(requestBody), requestType),
                responseType);
    }

    private <T> Mono<T> sendRequestWithRetry(WebClient.RequestHeadersSpec<?> request, Class<T> responseType) {
        return request
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + preferencesStorage.loadAccessToken())
                .retrieve()
                .onStatus(httpStatus -> httpStatus.value() == 401, response -> handleUnauthorized())
                .bodyToMono(responseType)
                .retryWhen(Retry.backoff(1, Duration.ofMillis(500)));
    }

    private Mono<? extends Throwable> handleUnauthorized() {
        var refreshToken = preferencesStorage.loadRefreshToken();

        if (refreshToken == null) {
            return Mono.error(new RuntimeException("Unauthorized and no refresh token available"));
        }

        return refresh(refreshToken).flatMap(response -> {
            preferencesStorage.saveTokens(response.getAccessToken(), response.getRefreshToken());
            return Mono.empty();
        });
    }

    private Mono<RefreshResponse> refresh(String refreshToken) {
        return webClient.post()
                .uri("/refresh")
                .bodyValue(new RefreshRequest(refreshToken))
                .retrieve()
                .bodyToMono(RefreshResponse.class)
                .doOnNext(response -> preferencesStorage.saveTokens(response.getAccessToken(), response.getRefreshToken())); // Обновляем токены
    }

    private Mono<Void> saveFile(byte[] fileData, String directory, String fileName) {
        return Mono.fromRunnable(() -> {
            try {
                var folder = new File(directory);

                if (!folder.exists()) {
                    folder.mkdirs();
                }

                var file = Paths.get(directory, fileName).toFile();

                try (var fos = new FileOutputStream(file)) {
                    fos.write(fileData);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error saving file: " + e.getMessage(), e);
            }
        });
    }
}
