package org.example.controllers;

import org.example.clients.VBoxClient;
import org.example.models.VirtualMachine;
import org.example.services.ComputeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/compute")
public class ComputeController {
    private ComputeService computeService;
    private VBoxClient vBoxClient;

    @Autowired
    public ComputeController(ComputeService computeService,
                             VBoxClient vBoxClient){
        this.computeService = computeService;
        this.vBoxClient = vBoxClient;
    }

    @PostMapping("/create")
    public VirtualMachine createVM()
    {
        vBoxClient.createVirtualMachine("FromIp");
        return new VirtualMachine();
    }
}
