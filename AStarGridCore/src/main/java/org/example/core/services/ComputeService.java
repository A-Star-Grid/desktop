package org.example.core.services;

import org.example.core.clients.ServerClient;
import org.example.core.clients.SshClient;
import org.example.core.configurations.AppSettings;
import org.example.core.models.ComputeResource;
import org.example.core.models.ComputeResult;
import org.example.core.models.ComputingTask;
import org.example.core.models.commands.docker.DockerManager;
import org.example.core.models.dto.SubscribeResponse;
import org.example.core.models.shedule.ScheduleTimeStamp;
import org.example.core.services.settings.ApplicationSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@Service
public class ComputeService {
    private final AppSettings appSettings;
    private final SubscribeService subscribeService;
    private final ServerClient serverClient;
    private final PreferencesStorage preferencesStorage;
    private final VirtualMachineFactory virtualMachineFactory;
    private final ApplicationSettingsService applicationSettingsService;

    private final ConcurrentHashMap<ComputingTask, Future<?>> runningTasks = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ConcurrentHashMap<UUID, String> results = new ConcurrentHashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(ComputeService.class);

    public ComputeService(AppSettings appSettings,
                          SubscribeService subscribeService,
                          ServerClient serverClient,
                          PreferencesStorage preferencesStorage,
                          ApplicationSettingsService applicationSettingsService,
                          VirtualMachineFactory virtualMachineFactory) {
        this.appSettings = appSettings;
        this.subscribeService = subscribeService;
        this.serverClient = serverClient;
        this.preferencesStorage = preferencesStorage;
        this.virtualMachineFactory = virtualMachineFactory;
        this.applicationSettingsService = applicationSettingsService;
    }

    @Scheduled(fixedDelayString = "${compute.process.interval}")
    public void process() {
        LOGGER.debug("POINT1");
        if (!applicationSettingsService.isComputationActive()) {
            cancelAllRunningTasks();
            virtualMachineFactory.stopVirtualMachine();
            return;
        }

        virtualMachineFactory.createVirtualMachineIfNotExist();

        var subscribes = subscribeService.getSubscribes();
        var currentTime = ScheduleTimeStamp.now();
        LOGGER.debug("POINT2");
        cancelOverdueTasks(currentTime);

        runTasksFromSubscribes(subscribes, currentTime);
    }


    private void cancelOverdueTasks(ScheduleTimeStamp currentTime) {
        LOGGER.debug("POINT3" + runningTasks.size());

        runningTasks.entrySet().removeIf(entry -> {
            LOGGER.debug("POINT5" + entry.getValue().isDone());

            if (entry.getValue().isDone()) {
                return true;
            }

            var task = entry.getKey();
            var future = entry.getValue();

            if (!task.getInterval().contains(currentTime)) {
                future.cancel(true);

                LOGGER.info("Cancelled task outside interval: " + task);

                return true;
            }

            return false;
        });
    }

    private void runTasksFromSubscribes(List<SubscribeResponse> subscribes, ScheduleTimeStamp currentTime) {
        for (var subscribe : subscribes) {
            for (var interval : subscribe.getScheduleIntervals()) {
                if (!interval.contains(currentTime)) {
                    continue;
                }

                var taskResponse = serverClient.getCurrentTask(
                        subscribe.getProjectId(),
                        preferencesStorage.getDeviceUUID()).block();

                if (taskResponse == null) {
                    continue;
                }

                var taskUuid = taskResponse.getTaskUuid();

                var key = new ComputingTask(subscribe.getProjectId(), interval, taskUuid);

                if (runningTasks.contains(key)) {
                    continue;
                }

                virtualMachineFactory.startVirtualMachineIfNotRunning();

                runningTasks.compute(key, (k, existingFuture) -> {
                    if (existingFuture != null && !existingFuture.isDone()) {
                        return existingFuture;
                    }

                    return executorService.submit(() -> {
                        if (!results.contains(taskUuid)) {
                            var result = computeTaskForProject(
                                    subscribe.getProjectId(),
                                    taskUuid,
                                    interval.getComputeResource());

                            if (result.getSuccess()) {
                                results.put(taskUuid, result.getResultPath());
                            } else {
                                LOGGER.info("Добавляем информацию о сбое на сервер");
                                serverClient.cancelProjectTask(
                                        subscribe.getProjectId(),
                                        preferencesStorage.getDeviceUUID(),
                                        taskUuid).block();
                            }
                        }

                        try {
                            uploadResult(taskUuid, subscribe.getProjectId(), results.get(taskUuid));
                            Files.deleteIfExists(Path.of(appSettings.taskArchivesDirectory, taskUuid.toString()));
                            results.remove(taskUuid);
                        } catch (IOException e) {
                            LOGGER.error("Error of upload task result " + e.getMessage());
                        }
                    });
                });

                LOGGER.debug("POINT4" + runningTasks.size());
            }
        }
    }

    private void cancelAllRunningTasks() {
        runningTasks.forEach((key, future) -> {
            if (!future.isDone()) {
                future.cancel(true);
                LOGGER.info("Cancel of the task: " + key.getProjectId());
            }
        });
        runningTasks.clear();
    }

    private ComputeResult computeTaskForProject(
            Integer projectId,
            UUID taskUuid,
            ComputeResource computeResource) {
        try {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            LOGGER.info("Start of compute for Project  " + projectId);

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

            var outputPath = computeTaskInTheDocker(
                    projectId,
                    taskUuid.toString(),
                    taskUuid + ".zip",
                    computeResource);

            Files.deleteIfExists(Path.of(projectDirectory.toString(), taskUuid + ".zip"));

            return new ComputeResult(outputPath, true) ;
        } catch (InterruptedException e) {
            LOGGER.info("Задача прервана: " + projectId);
            Thread.currentThread().interrupt(); // Восстанавливаем флаг

            return new ComputeResult(null, false);
        } catch (IOException e) {
            LOGGER.error("Ошибка чтения файлов " + projectId + ": " + e.getMessage());

            return new ComputeResult(null, false);
        } catch (Exception e) {
            LOGGER.error("Ошибка при обработке проекта "
                    + projectId + ": " + e.getMessage());

            return new ComputeResult(null, false);
        }
    }

    private String computeTaskInTheDocker(
            Integer projectId,
            String taskUuid,
            String archiveName,
            ComputeResource computeResource) throws InterruptedException {
        try {
            var sshClient = new SshClient(
                    virtualMachineFactory.createVirtualMachineIfNotExist().getIp(),
                    8022,
                    "root",
                    "1234");

            var dockerManager = new DockerManager(sshClient);

            var projectPath = "/mnt/shared/Project" + projectId;
            var taskPath = projectPath + "/" + taskUuid;
            var taskArchivePath = projectPath + "/" + archiveName;
            var resultDir = taskPath + "/output";
            var resultArchivePath = taskPath + "/output.zip";
            var dockerfilePath = "/root/";

            // Need after creating from ova image
            sshClient.executeCommand("rm /EMPTY");

            sshClient.executeCommand("unzip -o " + taskArchivePath + " -d " + projectPath + "/" + taskUuid);

            var containerName = "compute_project_" + projectId;
            dockerManager.startContainer(containerName, taskPath, dockerfilePath, computeResource);

            var waitFuture = dockerManager.waitForCompletion(containerName);

            try {
                waitFuture.get(); // Блокируемся
            } catch (InterruptedException e) {
                LOGGER.info("Поток выполенния получил прерывание — отменяем поток пингования контейнера");
                waitFuture.cancel(true);
                Thread.currentThread().interrupt();
                throw e;
            }

            sshClient.executeCommand("zip -r " + resultArchivePath + " " + resultDir);

            LOGGER.info("Результаты успешно сформированы.");

            return Path.of(appSettings.taskArchivesDirectory, "Project" + projectId, taskUuid, "output.zip").toString();
        } catch (ExecutionException e) {
            throw new RuntimeException("Error while compute in container " + e.getMessage());
        }
    }

    private void uploadResult(UUID taskUuid, Integer projectId, String outputPath) throws IOException {
        var uploadResult = serverClient.uploadResultArchive(
                taskUuid,
                projectId,
                preferencesStorage.getDeviceUUID(),
                outputPath).block();
        if(uploadResult.isSuccess()){
            LOGGER.info("Results sent");
            Files.deleteIfExists(Path.of(outputPath));
        } else {
            LOGGER.error("Error of upload result: " + uploadResult.getMessage());
        }
    }
}
