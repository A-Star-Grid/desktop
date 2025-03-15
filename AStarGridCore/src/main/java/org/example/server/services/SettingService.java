package org.example.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettingService {
    private final PreferencesStorage preferencesStorage;

    @Autowired
    public SettingService(PreferencesStorage preferencesStorage) {
        this.preferencesStorage = preferencesStorage;
    }

    public boolean setRamLimit(int ramMB) {
        return preferencesStorage.setRamLimit(ramMB);
    }

    public boolean setCpuLimit(double cpuCount) {
        return preferencesStorage.setCpuLimit(cpuCount);
    }

    public boolean setDiskLimit(int diskGB) {
        return preferencesStorage.setDiskLimit(diskGB);
    }

    public boolean setVirtualBoxPath(String path) {
        return preferencesStorage.setVirtualBoxPath(path);
    }

    public void setComputationActive(boolean isActive) {
        preferencesStorage.setComputationActive(isActive);
    }

    public int getRamLimit() {
        return preferencesStorage.getRamLimit();
    }

    public double getCpuLimit() {
        return preferencesStorage.getCpuLimit();
    }

    public int getDiskLimit() {
        return preferencesStorage.getDiskLimit();
    }

    public String getVirtualBoxPath() {
        return preferencesStorage.getVirtualBoxPath();
    }

    public boolean isComputationActive() {
        return preferencesStorage.isComputationActive();
    }

    public void resetToDefaults() {
        preferencesStorage.resetToDefaults();
    }
}
