package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.models.SubscribeRequest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerClient {
    private static final String BASE_URL = "http://localhost:8082";
    private final ObjectMapper objectMapper = new ObjectMapper();


    public String login(String username, String password) {
        String endpoint = BASE_URL + "/auth/login?username=" + username + "&password=" + password;
        return sendPostRequest(endpoint, "");
    }

    public String getProjects(int page, int perPage) {
        String endpoint = BASE_URL + "/project/list?page=" + page + "&perPage=" + perPage;
        return sendGetRequest(endpoint);
    }

    public String subscribeToProject(SubscribeRequest request) {
        String endpoint = BASE_URL + "/subscribes/subscribe";

        try {
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonBody = objectMapper.writeValueAsString(request);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes());
                os.flush();
            }

            return readResponse(conn);
        } catch (Exception e) {
            return "Ошибка запроса: " + e.getMessage();
        }
    }

    public String unsubscribeFromProject(int projectId) {
        String endpoint = BASE_URL + "/subscribes/unsubscribe?id=" + projectId;
        return sendPostRequest(endpoint, "");
    }

    public String getSubscribes() {
        String endpoint = BASE_URL + "/subscribes/subscribes_list";
        return sendGetRequest(endpoint);
    }

    private String sendGetRequest(String endpoint) {
        try {
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            return readResponse(conn);
        } catch (Exception e) {
            return "Error of request: " + e.getMessage();
        }
    }

    public String setRamLimit(int ram) {
        return sendPostRequest(BASE_URL + "/settings/ram?ramMB=" + ram, "");
    }

    public String setCpuLimit(double cpu) {
        return sendPostRequest(BASE_URL + "/settings/cpu?cpuCount=" + cpu, "");
    }

    public String setDiskLimit(int disk) {
        return sendPostRequest(BASE_URL + "/settings/disk?diskGB=" + disk, "");
    }

    public String setComputationState(boolean enable) {
        return sendPostRequest(BASE_URL + "/settings/computation?isActive=" + enable, "");
    }

    public String resetSettings() {
        return sendPostRequest(BASE_URL + "/settings/reset", "");
    }

    public String getCurrentSettings() {
        return sendGetRequest(BASE_URL + "/settings/current");
    }

    private String sendPostRequest(String endpoint, String body) {
        try {
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            if (!body.isEmpty()) {
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes());
                    os.flush();
                }
            }

            return readResponse(conn);
        } catch (Exception e) {
            return "Error of request: " + e.getMessage();
        }
    }

    private String readResponse(HttpURLConnection conn) {
        try {
            int responseCode = conn.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    responseCode >= 200 && responseCode < 300 ?
                            conn.getInputStream() : conn.getErrorStream()
            ));

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
            reader.close();

            return response.toString();
        } catch (Exception e) {
            return "Error of reading a answer: " + e.getMessage();
        }
    }
}
