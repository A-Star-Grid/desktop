package org.example.server.services;

import org.example.server.clients.ServerClient;
import org.example.server.clients.SshClient;
import org.example.server.clients.VBoxClient;
import org.example.server.configurations.AppSettings;
import org.example.server.models.ComputeResource;
import org.example.server.models.ComputingTask;
import org.example.server.models.VirtualMachine;
import org.example.server.models.commands.docker.DockerManager;
import org.example.server.models.shedule.ScheduleTimeStamp;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.*;

@Service
public class ComputeService {
    private final AppSettings appSettings;
    private final SubscribeService subscribeService;
    private final ServerClient serverClient;
    private final PreferencesStorage preferencesStorage;
    private final SettingService settingService;
    private final VBoxClient vBoxClient;
    private final VirtualMachine virtualMachine;
    private String virtualMachineName;

    private final ConcurrentHashMap<ComputingTask, Future<?>> runningTasks = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ConcurrentHashMap<UUID, String> results = new ConcurrentHashMap<>();

    public ComputeService(AppSettings appSettings,
                          SubscribeService subscribeService,
                          ServerClient serverClient,
                          PreferencesStorage preferencesStorage,
                          SettingService settingService,
                          VBoxClient vBoxClient,
                          VirtualMachine virtualMachine) {
        this.appSettings = appSettings;
        this.subscribeService = subscribeService;
        this.serverClient = serverClient;
        this.preferencesStorage = preferencesStorage;
        this.settingService = settingService;
        this.vBoxClient = vBoxClient;
        this.virtualMachine = virtualMachine;
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
            try {
                vBoxClient.addSharedFolderToVirtualMachine(virtualMachineName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        virtualMachine.setName(virtualMachineName);

        var subscribes = subscribeService.getSubscribes();
        var currentTime = ScheduleTimeStamp.now();

        for (var subscribe : subscribes) {
            for (var interval : subscribe.getScheduleIntervals()) {
                if (!interval.contains(currentTime)) {
                    continue;
                }

                var key = new ComputingTask(subscribe.getProjectId(), interval);

                if (runningTasks.contains(key)) {
                    continue;
                }

                var taskResponse = serverClient.getCurrentTask(
                        subscribe.getProjectId(),
                        preferencesStorage.getDeviceUUID()).block();

                if (taskResponse == null) {
                    continue;
                }

                var taskUuid = taskResponse.getTaskUuid();

                vBoxClient.startVirtualMachineIfNotRunning(virtualMachineName);


                runningTasks.compute(key, (k, existingFuture) -> {
                    if (existingFuture != null && !existingFuture.isDone()) {
                        return existingFuture;
                    }

                    return executorService.submit(() -> {
                        if (!results.contains(taskUuid)) {
                            var resultPath = startComputation(subscribe.getProjectId(), taskUuid, interval.getComputeResource());

                            if (resultPath != null) {
                                results.put(taskUuid, resultPath);
                            }
                        }

                        try {
                            uploadResult(taskUuid, subscribe.getProjectId(), results.get(taskUuid));
                            Files.deleteIfExists(Path.of(results.get(taskUuid)));
                            results.remove(taskUuid);
                        } catch (Exception e) {
                            System.out.println("Error of upload task result");
                        }
                    });
                });
            }
        }

        runningTasks.entrySet().removeIf(entry -> entry.getValue().isDone());
    }

    private void cancelAllRunningTasks() {
        runningTasks.forEach((key, future) -> {
            if (!future.isDone()) {
                future.cancel(true);
                System.out.println("Задача отменена: " + key.getProjectId());
            }
        });
        runningTasks.clear();
    }

    private String startComputation(Integer projectId, UUID taskUuid, ComputeResource computeResource) {
        try {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            System.out.println("Начато вычисление для проекта " + projectId);

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
            // TODO Need delete ip logic
            virtualMachine.setIp(vBoxClient.getVirtualMachineIp(virtualMachineName));


            var outputPath = runDockerComputation(
                    projectId,
                    taskUuid.toString(),
                    taskUuid + ".zip",
                    computeResource);


            Files.deleteIfExists(Path.of(projectDirectory.toString(), taskUuid + ".zip"));

            return outputPath;
        } catch (InterruptedException e) {
            System.out.println("⚠️ Задача прервана: " + projectId);
            Thread.currentThread().interrupt(); // Восстанавливаем флаг
            return null;
        } catch (IOException e) {
            System.out.println("⚠️ Ошибка чтения файлов " + projectId);
            throw new RuntimeException(e);
        } catch (Exception e) {
            System.err.println("⚠️ Ошибка при обработке проекта "
                    + projectId + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private String runDockerComputation(
            Integer projectId,
            String taskUuid,
            String archiveName,
            ComputeResource computeResource) {
        try {
            var sshClient = new SshClient("localhost",8022, "root", "1234");

            var dockerManager = new DockerManager(sshClient);

            var projectPath = "/mnt/shared/Project" + projectId;
            var taskArchivePath = projectPath + "/" + archiveName;
            var resultDir = projectPath + "/output";
            var resultArchivePath = projectPath + "/output.zip";
            var dockerfilePath = "/root/";
            var taskPath = projectPath + "/" + taskUuid;

            // Need after creating from ova
            sshClient.executeCommand("rm /EMPTY");

            sshClient.executeCommand("unzip -o " + taskArchivePath + " -d " + taskPath);

            var containerName = "compute_project_" + projectId;
            dockerManager.startContainer(containerName, taskPath, dockerfilePath, computeResource);

            dockerManager.waitForCompletion(containerName).thenRun(() -> {
                try {
                    sshClient.executeCommand("rm -f " + resultArchivePath);
                    sshClient.executeCommand("zip -r " + resultArchivePath + " " + projectPath);
                    sshClient.executeCommand("rm -f " + resultDir);

                    System.out.println("Результаты успешно сформированы.");
                } catch (Exception e) {
                    System.err.println("Ошибка при обработке результата: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            return Path.of(appSettings.taskArchivesDirectory, "Project" + projectId, "output.zip").toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Container was not run");
        }
    }

    private void uploadResult(UUID taskUuid, Integer projectId, String outputPath) throws IOException {
        var uploadResult = serverClient.uploadResultArchive(
                taskUuid,
                projectId,
                preferencesStorage.getDeviceUUID(),
                outputPath).block();

        Files.deleteIfExists(Path.of(outputPath));
        System.out.println("Results sent");
    }
}
