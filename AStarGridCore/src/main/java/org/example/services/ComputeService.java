package org.example.services;

import org.example.clients.ServerClient;
import org.example.configurations.AppSettings;
import org.example.models.ComputingTask;
import org.example.models.shedule.ScheduleInterval;
import org.example.models.shedule.ScheduleTimeStamp;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class ComputeService {
    private AppSettings appSettings;
    private SubscribeService subscribeService;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ServerClient serverClient;
    private final PreferencesStorage preferencesStorage;
    private final SettingService settingService;

    public ComputeService(AppSettings appSettings,
                          SubscribeService subscribeService,
                          ServerClient serverClient,
                          PreferencesStorage preferencesStorage,
                          SettingService settingService) {
        this.appSettings = appSettings;
        this.subscribeService = subscribeService;
        this.serverClient = serverClient;
        this.preferencesStorage = preferencesStorage;
        this.settingService = settingService;
    }

    private final ConcurrentHashMap<ComputingTask, Future<?>> runningTasks = new ConcurrentHashMap<>();

    @Scheduled(fixedRateString = "${compute.process.interval}")
    public void process() {
        if (!settingService.isComputationActive()) {
            cancelAllRunningTasks();
            return;
        }

        var subscribes = subscribeService.getSubscribes();
        var currentTime = ScheduleTimeStamp.now();

        for (var subscribe : subscribes) {
            for (var interval : subscribe.getScheduleIntervals()) {
                if (interval.contains(currentTime)) {
                    var key = new ComputingTask(subscribe.getProjectId(), interval);

                    runningTasks.compute(key, (k, existingFuture) -> {
                        if (existingFuture != null && !existingFuture.isDone()) {
                            return existingFuture;
                        }

                        var newFuture = executorService.submit(() -> {
                            startComputation(subscribe.getProjectId(), interval);
                        });

                        return newFuture;
                    });
                }
            }
        }

        runningTasks.entrySet().removeIf(entry -> entry.getValue().isDone());
    }

    private void cancelAllRunningTasks() {
        runningTasks.forEach((key, future) -> {
            if (!future.isDone()) {
                future.cancel(true);
                System.out.println("⛔ Задача отменена: " + key.getProjectId());
            }
        });
        runningTasks.clear();
    }

    private void startComputation(Integer projectId, ScheduleInterval interval) {
        try {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            System.out.println("⏳ Начато вычисление для проекта " + projectId);

            var taskResponse = serverClient.getCurrentTask(projectId, preferencesStorage.getDeviceUUID()).block();
            var taskUuid = taskResponse.getTaskUuid();

            var projectDirectory = Paths.get(
                    appSettings.taskArchivesDirectory,
                    "Project" + projectId
            );

            if (!Files.exists(projectDirectory)) {
                Files.createDirectories(projectDirectory);
            }

            var archivePath = projectDirectory.resolve(taskUuid + ".zip");

            if (!Files.exists(archivePath)) {
                serverClient.downloadTaskArchive(
                        taskUuid,
                        projectDirectory.toString(),
                        taskUuid + ".zip"
                ).block();
            }

            Thread.sleep(10000);

            System.out.println("✅ Завершено вычисление для проекта " + projectId);
        } catch (InterruptedException e) {
            System.out.println("⚠️ Задача прервана: " + projectId);
            cleanupAfterCancel(projectId);
            Thread.currentThread().interrupt(); // Восстанавливаем флаг
        } catch (IOException e) {
            System.out.println("⚠️ Ошибка чтения файлов " + projectId);
            throw new RuntimeException(e);
        } catch (Exception e) {
            System.err.println("⚠️ Ошибка при обработке проекта "
                    + projectId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cleanupAfterCancel(Integer projectId) {
        System.out.println("🧹 Очистка ресурсов проекта " + projectId);
    }
}
