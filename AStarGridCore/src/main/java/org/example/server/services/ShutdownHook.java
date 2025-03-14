package org.example.server.services;

import org.example.server.clients.VBoxClient;
import org.example.server.models.VirtualMachine;
import org.springframework.stereotype.Component;
import jakarta.annotation.PreDestroy;

@Component
public class ShutdownHook {
    private VBoxClient vBoxClient;
    private VirtualMachine virtualMachine;

    public ShutdownHook(VBoxClient vBoxClient,
                        VirtualMachine virtualMachine){
        this.vBoxClient = vBoxClient;
        this.virtualMachine = virtualMachine;
    }

    @PreDestroy
    public void onShutdown() {
        System.out.println("Stopping of Virtual Machine" +  virtualMachine.getName());
        vBoxClient.stopVirtualMachine(virtualMachine.getName());
    }
}

