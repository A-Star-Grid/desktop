package org.example.core.services.settings;

import org.example.core.services.PreferencesStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicationSettingsService {
    private PreferencesStorage preferencesStorage;

    @Autowired
    public ApplicationSettingsService(PreferencesStorage preferencesStorage){
        this.preferencesStorage = preferencesStorage;
    }

    public boolean setVirtualBoxPath(String path) {
        return preferencesStorage.setVirtualBoxPath(path);
    }

    public void setComputationActive(boolean isActive) {
        preferencesStorage.setComputationActive(isActive);
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
