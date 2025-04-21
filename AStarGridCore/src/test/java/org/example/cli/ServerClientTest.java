package org.example.cli;

import org.example.cli.models.SubscribeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ServerClientTest {
    private HttpClient httpClient;
    private ServerClient serverClient;

    @BeforeEach
    void setUp() {
        httpClient = mock(HttpClient.class);
        serverClient = new ServerClient(httpClient);
    }

    @Test
    void login_success() {
        when(httpClient.post(contains("/auth/login"), anyString()))
                .thenReturn("ok");

        String result = serverClient.login("admin", "pass");
        assertEquals("ok", result);
    }

    @Test
    void getProjects_returnsJson() {
        when(httpClient.get(contains("/project/list")))
                .thenReturn("{projects:[]}");

        String result = serverClient.getProjects(1, 10);
        assertTrue(result.contains("projects"));
    }

    @Test
    void setRamLimit_callsCorrectEndpoint() {
        when(httpClient.post(contains("/settings/ram"), anyString()))
                .thenReturn("ok");

        String result = serverClient.setRamLimit(1024);
        assertEquals("ok", result);
    }

    @Test
    void subscribeToProject_serializesJson() {
        var request = new SubscribeRequest(1, new ArrayList<>());
        when(httpClient.post(contains("/subscribes/subscribe"), anyString()))
                .thenReturn("subscribed");

        String result = serverClient.subscribeToProject(request);
        assertEquals("subscribed", result);
    }
}
