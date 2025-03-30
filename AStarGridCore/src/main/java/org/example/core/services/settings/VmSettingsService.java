package org.example.core.services.settings;

import org.example.core.services.PreferencesStorage;
import org.example.core.services.VirtualMachineFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VmSettingsService {
    private final PreferencesStorage preferencesStorage;
    private final VirtualMachineFactory virtualMachineFactory;

    @Autowired
    public VmSettingsService(PreferencesStorage preferencesStorage,
                             VirtualMachineFactory virtualMachineFactory) {
        this.preferencesStorage = preferencesStorage;
        this.virtualMachineFactory = virtualMachineFactory;
    }

    public boolean setRamLimit(int ramMB) {
        virtualMachineFactory.setVirtualMachineRam(ramMB);
        return preferencesStorage.setRamLimit(ramMB);
    }

    public boolean setCpuLimit(Integer cpuCount) {
        virtualMachineFactory.setVirtualMachineCpu(cpuCount);
        return preferencesStorage.setCpuLimit(cpuCount);
    }

    public boolean setDiskLimit(int diskGB) {
        return preferencesStorage.setDiskLimit(diskGB);
    }

    public int getRamLimit() {
        return preferencesStorage.getRamLimit();
    }

    public int getCpuLimit() {
        return preferencesStorage.getCpuLimit();
    }

    public int getDiskLimit() {
        return preferencesStorage.getDiskLimit();
    }
}
