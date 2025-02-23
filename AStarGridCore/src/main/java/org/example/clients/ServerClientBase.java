package org.example.clients;

import org.example.configurations.AppSettings;
import org.example.models.dto.RefreshRequest;
import org.example.models.dto.RefreshResponse;
import org.example.services.PreferencesStorage;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

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

    protected <T> Mono<T> postWithRetry(String uri, Class<T> responseType) {
        return sendRequestWithRetry(webClient.post().uri(uri), responseType);
    }

    /**
     * Перегруженная версия getWithRetry с параметрами запроса
     */
    protected <T> Mono<T> getWithRetry(String uri, Mono<Map<String, String>> queryParams, Class<T> responseType) {
        return queryParams.flatMap(params -> {
            WebClient.RequestHeadersUriSpec<?> uriSpec = webClient.get();
            WebClient.RequestHeadersSpec<?> requestSpec = uriSpec.uri(builder -> {
                builder.path(uri);
                params.forEach(builder::queryParam);
                return builder.build();
            });

            return sendRequestWithRetry(requestSpec, responseType);
        });
    }

    /**
     * Перегруженная версия postWithRetry с параметрами запроса
     */
    protected <T> Mono<T> postWithRetry(String uri, Mono<Map<String, String>> queryParams, Class<T> responseType) {
        return queryParams.flatMap(params -> {
            WebClient.UriSpec<WebClient.RequestBodySpec> uriSpec = webClient.post();
            WebClient.RequestBodySpec requestSpec = uriSpec.uri(builder -> {
                builder.path(uri);
                params.forEach(builder::queryParam);
                return builder.build();
            });
            return sendRequestWithRetry(requestSpec, responseType);
        });
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
}
