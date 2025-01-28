package org.example.controllers;

import org.example.services.SubscribeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subscribe")
public class SubscribeController {
    private SubscribeService subscribeService;

    @Autowired
    public SubscribeController(SubscribeService subscribeService){
        this.subscribeService = subscribeService;
    }
}
