package org.example.controllers;

import org.example.services.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/project")
public class ProjectController {
    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/list")
    public ResponseEntity<String> getProjects(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int perPage) {
        return projectService.getProjects(page, perPage);
    }
}
