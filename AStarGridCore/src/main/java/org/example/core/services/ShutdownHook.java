package org.example.core.services;

import org.springframework.stereotype.Component;
import jakarta.annotation.PreDestroy;

@Component
public class ShutdownHook {
    private VirtualMachineFactory virtualMachineFactory;

    public ShutdownHook(VirtualMachineFactory virtualMachineFactory){
        this.virtualMachineFactory = virtualMachineFactory;
    }

    @PreDestroy
    public void onShutdown() {
        var virtualMachine = virtualMachineFactory.getVirtualMachine();
        if(virtualMachine.isEmpty()){
            return;
        }

        System.out.println("Stopping of Virtual Machine" +  virtualMachine.get().getName());

        virtualMachineFactory.stopVirtualMachine();
    }
}

