package org.example.services;

import org.example.clients.ServerClient;
import org.example.models.dto.Project;
import org.example.models.dto.ProjectsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {
    private final ServerClient serverClient;
    private final PreferencesStorage preferencesStorage;

    @Autowired
    public ProjectService(ServerClient serverClient, PreferencesStorage preferencesStorage) {
        this.serverClient = serverClient;
        this.preferencesStorage = preferencesStorage;
    }

    public ProjectsResponse getProjects(int page, int perPage) {
        var projectsMono = serverClient.getProjects(page, perPage);
        var projects = projectsMono.block();

        return projects;
    }

    public List<Project> getProjectById(int id) {
        var projectsMono = serverClient.getProjects();
        var projects = projectsMono.block();

        return projects.getProjects().stream().filter(p -> p.getId() == id).toList();
    }

    public List<Project> getProjectByNamePattern(String namePattern) {
        var projectsMono = serverClient.getProjects();
        var projects = projectsMono.block();

        return projects.getProjects().stream().filter(p -> p.getName().contains(namePattern)).toList();
    }
}
