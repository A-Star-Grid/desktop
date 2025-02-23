package org.example.services;

import org.example.clients.ServerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ProjectService {
    private final ServerClient serverClient;
    private final PreferencesStorage preferencesStorage;

    @Autowired
    public ProjectService(ServerClient serverClient, PreferencesStorage preferencesStorage) {
        this.serverClient = serverClient;
        this.preferencesStorage = preferencesStorage;
    }

    public ResponseEntity<String> getProjects(int page, int perPage) {
        Mono<String> projectsMono = serverClient.getProjects(page, perPage);
        String projects = projectsMono.block(); // Блокируем, чтобы получить результат

        if (projects != null) {
            return ResponseEntity.ok(projects);
        } else {
            return ResponseEntity.status(500).body("Failed to fetch projects");
        }
    }
}
