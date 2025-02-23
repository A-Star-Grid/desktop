package org.example.services;

import org.example.clients.ServerClient;
import org.example.models.dto.SubscribeRequest;
import org.example.models.dto.SubscribeResponse;
import org.example.models.dto.SubscribeTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.util.List;

public class SubscribeService {
    private final ServerClient serverClient;
    private final PreferencesStorage preferencesStorage;

    @Autowired
    public SubscribeService(ServerClient serverClient, PreferencesStorage preferencesStorage) {
        this.serverClient = serverClient;
        this.preferencesStorage = preferencesStorage;
    }

    public ResponseEntity<SubscribeResponse> subscribe(SubscribeRequest subscribeRequest) {
        var projectsMono = serverClient.subscribeToProject(new SubscribeTransport(subscribeRequest, preferencesStorage.getDeviceUUID()));
        var projects = projectsMono.block();

        return ResponseEntity.ok(projects);
    }

    public ResponseEntity<String> unsubscribeFromProject(int id) {
        Mono<String> projectsMono = serverClient.unsubscribeFromProject(id);
        String projects = projectsMono.block();

        if (projects != null) {
            return ResponseEntity.ok(projects);
        } else {
            return ResponseEntity.status(500).body("Failed to fetch projects");
        }
    }

    public ResponseEntity<List<SubscribeResponse>> getSubscribes() {
        var projectsMono = serverClient.getSubscribes();
        var projects = projectsMono.block();

        return ResponseEntity.ok(projects);
    }
}
