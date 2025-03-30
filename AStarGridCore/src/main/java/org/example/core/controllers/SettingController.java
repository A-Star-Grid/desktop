package org.example.core.controllers;

import org.example.core.models.dto.CurrentSettingsResponse;
import org.example.core.services.settings.ApplicationSettingsService;
import org.example.core.services.settings.VmSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settings")
public class SettingController {

    private final VmSettingsService vmSettingsService;
    private final ApplicationSettingsService applicationSettingsService;

    @Autowired
    public SettingController(VmSettingsService vmSettingsService,
                             ApplicationSettingsService applicationSettingsService) {
        this.vmSettingsService = vmSettingsService;
        this.applicationSettingsService = applicationSettingsService;
    }

    @PostMapping("/ram")
    public ResponseEntity<String> setRamLimit(@RequestParam Integer ramMB) {
        if (vmSettingsService.setRamLimit(ramMB)) {
            return ResponseEntity.ok("RAM limit set successfully.");
        }
        return ResponseEntity.badRequest().body("Invalid RAM limit.");
    }

    @PostMapping("/cpu")
    public ResponseEntity<String> setCpuLimit(@RequestParam Integer cpuCount) {
        if (vmSettingsService.setCpuLimit(cpuCount)) {
            return ResponseEntity.ok("CPU limit set successfully.");
        }
        return ResponseEntity.badRequest().body("Invalid CPU limit.");
    }

    @PostMapping("/disk")
    public ResponseEntity<String> setDiskLimit(@RequestParam int diskGB) {
        if (vmSettingsService.setDiskLimit(diskGB)) {
            return ResponseEntity.ok("Disk space limit set successfully.");
        }
        return ResponseEntity.badRequest().body("Invalid disk limit.");
    }

    @PostMapping("/virtualbox")
    public ResponseEntity<String> setVirtualBoxPath(@RequestParam String path) {
        if (applicationSettingsService.setVirtualBoxPath(path)) {
            return ResponseEntity.ok("VirtualBox path set successfully.");
        }
        return ResponseEntity.badRequest().body("Invalid VirtualBox path.");
    }

    @PostMapping("/computation")
    public ResponseEntity<String> setComputationState(@RequestParam boolean isActive) {
        applicationSettingsService.setComputationActive(isActive);
        return ResponseEntity.ok("Computation state updated.");
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetSettings() {
        applicationSettingsService.resetToDefaults();
        return ResponseEntity.ok("Settings reset to default values.");
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentSettings() {
        return ResponseEntity.ok(new CurrentSettingsResponse(
                vmSettingsService.getRamLimit(),
                vmSettingsService.getCpuLimit(),
                vmSettingsService.getDiskLimit(),
                applicationSettingsService.getVirtualBoxPath(),
                applicationSettingsService.isComputationActive()
        ));
    }
}
