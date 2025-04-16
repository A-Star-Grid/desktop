package org.example.core.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import jakarta.annotation.PreDestroy;

@Component
public class ShutdownHook {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownHook.class);

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

        LOGGER.info("Stopping of Virtual Machine" +  virtualMachine.get().getName());

        virtualMachineFactory.stopVirtualMachine();
    }
}

