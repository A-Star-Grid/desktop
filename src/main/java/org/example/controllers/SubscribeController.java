package org.example.controllers;

import org.example.models.dto.SubscribeRequest;
import org.example.models.dto.SubscribeResponse;
import org.example.services.SubscribeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public class SubscribeController {
    SubscribeService subscribeService;

    @Autowired
    public SubscribeController(SubscribeService subscribeService) {
        this.subscribeService = subscribeService;
    }

    @PostMapping("/subscribe")
    public ResponseEntity<SubscribeResponse> subscribeToProject(@RequestBody SubscribeRequest request) {
        return subscribeService.subscribe(request);
    }

    @GetMapping("/subscribes_list")
    public ResponseEntity<List<SubscribeResponse>> getSubscribes() {
        return subscribeService.getSubscribes();
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<String> unsubscribeFromProject(
            @RequestParam() int id) {
        return subscribeService.unsubscribeFromProject(id);
    }
}
