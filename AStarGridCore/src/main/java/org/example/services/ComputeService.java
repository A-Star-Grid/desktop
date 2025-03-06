package org.example.services;

import org.example.clients.ServerClient;
import org.example.clients.SshClient;
import org.example.clients.VBoxClient;
import org.example.configurations.AppSettings;
import org.example.models.ComputingTask;
import org.example.models.commands.docker.DockerManager;
import org.example.models.shedule.ScheduleInterval;
import org.example.models.shedule.ScheduleTimeStamp;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        if (vBoxClient.createVirtualMachineIfNotExist(virtualMachineName)) {
            vBoxClient.addSharedFolderToVirtualMachine(virtualMachineName);
        }

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

            var ip = vBoxClient.getVirtualMachineIp(virtualMachineName);
            System.out.println(ip);

            runDockerComputation(projectId, ip, taskUuid.toString(), taskUuid + ".zip");

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

    private void runDockerComputation(Integer projectId, String virtualMachineIp, String task_uuid, String archiveName) {
        try {
            // 1. Создаем SSH-клиента
            var sshClient = new SshClient(virtualMachineIp, "zemlianin", "1234");

            // 2. Создаем DockerManager
            var dockerManager = new DockerManager(sshClient);

            // 3. Определяем пути
            var projectPath = "/mnt/shared/" + "Project" + projectId;
            var taskArchivePath = projectPath + "/" + archiveName;
            var resultDir = projectPath + "/output"; // Директория результатов
            var resultArchivePath = resultDir + ".zip"; // Итоговый архив
            var dockerfilePath = "/home/zemlianin/";
            var taskPath = projectPath + "/" + task_uuid;

            // 4. Разархивируем задание внутри виртуальной машины
            sshClient.executeCommand("unzip -o " + taskArchivePath + " -d " + taskPath);

            // 5. Запускаем контейнер
            var containerName = "compute_project_" + projectId;
            dockerManager.startContainer(containerName, taskPath, dockerfilePath);

            // 6. Ожидаем завершения контейнера
            dockerManager.waitForCompletion(containerName).thenRun(() -> {
                try {
                    // 7. Архивируем результат в ZIP
                    sshClient.executeCommand("rm -f " + resultArchivePath); // Удаляем старый архив, если он есть
                    sshClient.executeCommand("zip -r " + resultArchivePath + " " + resultDir);

                    // 8. Отправляем результаты на сервер
                    //          serverClient.uploadComputationResult(projectId, preferencesStorage.getDeviceUUID(), resultArchivePath).block();

                    System.out.println("📤 Результаты успешно отправлены на сервер.");
                } catch (Exception e) {
                    System.err.println("⚠ Ошибка при обработке результата: " + e.getMessage());
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            System.err.println("⚠ Ошибка при запуске контейнера для проекта " + projectId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cleanupAfterCancel(Integer projectId) {
        System.out.println("🧹 Очистка ресурсов проекта " + projectId);
        System.out.println("🧹 Завершена очистка ресурсов проекта " + projectId);
    }
}
