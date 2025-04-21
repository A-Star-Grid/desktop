package org.example.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.cli.models.SubscribeRequest;

public class ServerClient {
    private static final String BASE_URL = "http://localhost:8082";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;

    public ServerClient() {
        this.httpClient = new DefaultHttpClient();
    }

    public ServerClient(HttpClient client) {
        this.httpClient = client;
    }

    public String login(String username, String password) {
        return httpClient.post(BASE_URL + "/auth/login?username=" + username + "&password=" + password, "");
    }

    public String getProjects(int page, int perPage) {
        return httpClient.get(BASE_URL + "/project/list?page=" + page + "&perPage=" + perPage);
    }

    public String subscribeToProject(SubscribeRequest request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            return httpClient.post(BASE_URL + "/subscribes/subscribe", json);
        } catch (Exception e) {
            return "Ошибка запроса: " + e.getMessage();
        }
    }

    public String unsubscribeFromProject(int projectId) {
        return httpClient.post(BASE_URL + "/subscribes/unsubscribe?id=" + projectId, "");
    }

    public String getSubscribes() {
        return httpClient.get(BASE_URL + "/subscribes/subscribes_list");
    }

    public String setRamLimit(int ram) {
        return httpClient.post(BASE_URL + "/settings/ram?ramMB=" + ram, "");
    }

    public String setCpuLimit(double cpu) {
        return httpClient.post(BASE_URL + "/settings/cpu?cpuCount=" + cpu, "");
    }

    public String setDiskLimit(int disk) {
        return httpClient.post(BASE_URL + "/settings/disk?diskGB=" + disk, "");
    }

    public String setComputationState(boolean enable) {
        return httpClient.post(BASE_URL + "/settings/computation?isActive=" + enable, "");
    }

    public String resetSettings() {
        return httpClient.post(BASE_URL + "/settings/reset", "");
    }

    public String getCurrentSettings() {
        return httpClient.get(BASE_URL + "/settings/current");
    }
}
