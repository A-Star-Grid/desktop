package org.example.server.controllers;

import org.example.server.models.dto.Project;
import org.example.server.models.dto.ProjectsResponse;
import org.example.server.services.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/project")
public class ProjectController {
    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/list")
    public ResponseEntity<ProjectsResponse> getProjects(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int perPage) {
        return ResponseEntity.ok(projectService.getProjects(page, perPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<Project>> getProjectById(@PathVariable int id) {
        var projects = projectService.getProjectById(id);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Project>> getProjectByName(@RequestParam String name) {
        var projects = projectService.getProjectByNamePattern(name);
        return ResponseEntity.ok(projects);
    }
}
