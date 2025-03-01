package org.example.services;

import org.example.clients.ServerClient;
import org.example.clients.VBoxClient;
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
    private final AppSettings appSettings;
    private final SubscribeService subscribeService;
    private final ServerClient serverClient;
    private final PreferencesStorage preferencesStorage;
    private final SettingService settingService;
    private final VBoxClient vBoxClient;
    private String virtualMachineName;

    private final ConcurrentHashMap<ComputingTask, Future<?>> runningTasks = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public ComputeService(AppSettings appSettings,
                          SubscribeService subscribeService,
                          ServerClient serverClient,
                          PreferencesStorage preferencesStorage,
                          SettingService settingService,
                          VBoxClient vBoxClient) {
        this.appSettings = appSettings;
        this.subscribeService = subscribeService;
        this.serverClient = serverClient;
        this.preferencesStorage = preferencesStorage;
        this.settingService = settingService;
        this.vBoxClient = vBoxClient;
    }

    @Scheduled(fixedDelayString = "${compute.process.interval}")
    public void process() {
        if (!settingService.isComputationActive()) {
            cancelAllRunningTasks();
            vBoxClient.stopVirtualMachine(virtualMachineName);
            return;
        }

        virtualMachineName = "vm-" + preferencesStorage.getDeviceUUID().toString();
        vBoxClient.createVirtualMachineIfNotExist(virtualMachineName);

        var subscribes = subscribeService.getSubscribes();
        var currentTime = ScheduleTimeStamp.now();

        for (var subscribe : subscribes) {
            for (var interval : subscribe.getScheduleIntervals()) {
                if (interval.contains(currentTime)) {
                    vBoxClient.startVirtualMachineIfNotRunning(virtualMachineName);

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
        System.out.println("🧹 Завершена очистка ресурсов проекта " + projectId);
    }
}
