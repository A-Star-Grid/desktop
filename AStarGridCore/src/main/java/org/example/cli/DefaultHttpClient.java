package org.example.cli;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DefaultHttpClient implements HttpClient {
    @Override
    public String get(String url) {
        try {
            var conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            return readResponse(conn);
        } catch (Exception e) {
            return "Error of request: " + e.getMessage();
        }
    }

    @Override
    public String post(String url, String body) {
        try {
            var conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            if (!body.isEmpty()) {
                try (var os = conn.getOutputStream()) {
                    os.write(body.getBytes());
                }
            }
            return readResponse(conn);
        } catch (Exception e) {
            return "Error of request: " + e.getMessage();
        }
    }

    private String readResponse(HttpURLConnection conn) {
        try {
            var reader = new BufferedReader(new InputStreamReader(
                    conn.getResponseCode() >= 200 && conn.getResponseCode() < 300
                            ? conn.getInputStream()
                            : conn.getErrorStream()));
            return reader.lines().reduce("", (acc, line) -> acc + line + "\n");
        } catch (Exception e) {
            return "Error of reading a answer: " + e.getMessage();
        }
    }
}

