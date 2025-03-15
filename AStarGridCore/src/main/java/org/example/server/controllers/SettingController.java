package org.example.server.controllers;

import org.example.server.models.dto.CurrentSettingsResponse;
import org.example.server.services.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settings")
public class SettingController {

    private final SettingService settingService;

    @Autowired
    public SettingController(SettingService settingService) {
        this.settingService = settingService;
    }

    @PostMapping("/ram")
    public ResponseEntity<String> setRamLimit(@RequestParam int ramMB) {
        if (settingService.setRamLimit(ramMB)) {
            return ResponseEntity.ok("RAM limit set successfully.");
        }
        return ResponseEntity.badRequest().body("Invalid RAM limit.");
    }

    @PostMapping("/cpu")
    public ResponseEntity<String> setCpuLimit(@RequestParam double cpuCount) {
        if (settingService.setCpuLimit(cpuCount)) {
            return ResponseEntity.ok("CPU limit set successfully.");
        }
        return ResponseEntity.badRequest().body("Invalid CPU limit.");
    }

    @PostMapping("/disk")
    public ResponseEntity<String> setDiskLimit(@RequestParam int diskGB) {
        if (settingService.setDiskLimit(diskGB)) {
            return ResponseEntity.ok("Disk space limit set successfully.");
        }
        return ResponseEntity.badRequest().body("Invalid disk limit.");
    }

    @PostMapping("/virtualbox")
    public ResponseEntity<String> setVirtualBoxPath(@RequestParam String path) {
        if (settingService.setVirtualBoxPath(path)) {
            return ResponseEntity.ok("VirtualBox path set successfully.");
        }
        return ResponseEntity.badRequest().body("Invalid VirtualBox path.");
    }

    @PostMapping("/computation")
    public ResponseEntity<String> setComputationState(@RequestParam boolean isActive) {
        settingService.setComputationActive(isActive);
        return ResponseEntity.ok("Computation state updated.");
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetSettings() {
        settingService.resetToDefaults();
        return ResponseEntity.ok("Settings reset to default values.");
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentSettings() {
        return ResponseEntity.ok(new CurrentSettingsResponse(
                settingService.getRamLimit(),
                settingService.getCpuLimit(),
                settingService.getDiskLimit(),
                settingService.getVirtualBoxPath(),
                settingService.isComputationActive()
        ));
    }
}
