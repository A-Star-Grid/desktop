package org.example.services;

import org.example.clients.ServerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ProjectService {
    private final ServerClient serverClient;

    @Autowired
    public ProjectService(ServerClient serverClient) {
        this.serverClient = serverClient;
    }

    public ResponseEntity<String> getProjects(int page, int perPage) {

        Mono<String> projectsMono = serverClient.getProjects(token, page, perPage);
        String projects = projectsMono.block(); // Блокируем, чтобы получить результат

        if (projects != null) {
            return ResponseEntity.ok(projects);
        } else {
            return ResponseEntity.status(500).body("Failed to fetch projects");
        }
    }
}
