package org.example.services;

import org.example.configurations.AppSettings;
import org.example.models.shedule.ScheduleInterval;
import org.example.models.shedule.ScheduleTimeStamp;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ComputeService {
    private AppSettings appSettings;
    private SubscribeService subscribeService;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public ComputeService(AppSettings appSettings,
                          SubscribeService subscribeService) {
        this.appSettings = appSettings;
        this.subscribeService = subscribeService;
    }

    @Scheduled(fixedRateString = "${compute.process.interval}")
    public void process() {
        var subscribes = subscribeService.getSubscribes();
        var currentTime = ScheduleTimeStamp.now();

        for (var subscribe : subscribes) {
            for (var interval : subscribe.getScheduleIntervals()) {
                if (interval.contains(currentTime)) {
                    startComputation(subscribe.getProjectId(), interval);
                }
            }
        }
    }

    private void startComputation(Integer projectId, ScheduleInterval interval) {
        System.out.println("⏳ Начато вычисление для проекта " + projectId +
                " (CPU: " + interval.getComputeResource().getCpuCores() +
                ", RAM: " + interval.getComputeResource().getRam() +
                "MB, Disk: " + interval.getComputeResource().getDiskSpace() + "GB)");

        executorService.submit(() -> {
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            System.out.println("✅ Завершено вычисление для проекта " + projectId);
        });
    }
}
