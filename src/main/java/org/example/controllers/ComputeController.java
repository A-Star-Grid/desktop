package org.example.controllers;

import org.example.services.ComputeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/compute")
public class ComputeController {
    private ComputeService computeService;

    @Autowired
    public ComputeController(ComputeService computeService){
        this.computeService = computeService;
    }
}
