package org.example.services;

import org.example.configurations.AppSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ComputeService {
    private AppSettings appSettings;

    public ComputeService(AppSettings appSettings){
        this.appSettings =appSettings;
    }

    @Scheduled(fixedRateString = "${compute.process.interval}")
    public void process() {
        System.out.println("Executing process at: " + System.currentTimeMillis());
    }
}
