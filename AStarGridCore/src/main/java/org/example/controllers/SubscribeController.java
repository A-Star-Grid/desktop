package org.example.controllers;

import org.example.models.dto.SubscribeRequest;
import org.example.models.dto.SubscribeResponse;
import org.example.services.SubscribeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subscribes")
public class SubscribeController {
    SubscribeService subscribeService;

    @Autowired
    public SubscribeController(SubscribeService subscribeService) {
        this.subscribeService = subscribeService;
    }

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribeToProject(@RequestBody SubscribeRequest request) {
        return ResponseEntity.ok(subscribeService.subscribe(request));
    }

    @GetMapping("/subscribes_list")
    public ResponseEntity<List<SubscribeResponse>> getSubscribes() {
        return ResponseEntity.ok(subscribeService.getSubscribes());
    }

    @GetMapping("/subscribes_by_project_id")
    public ResponseEntity<List<SubscribeResponse>> getSubscribesByProjectId(@PathVariable int id) {
        return ResponseEntity.ok(subscribeService.getSubscribesByProjectId(id));
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<String> unsubscribeFromProject(
            @RequestParam() int id) {
        return subscribeService.unsubscribeFromProject(id);
    }
}
