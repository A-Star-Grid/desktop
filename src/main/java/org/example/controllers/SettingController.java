package org.example.controllers;

import org.example.services.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/settings")
public class SettingController {

    private SettingService settingService;

    @Autowired
    public SettingController(SettingService settingService){
        this.settingService = settingService;
    }
}
