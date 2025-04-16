package org.example.core.models.commands.docker;

import org.example.core.clients.SshClient;
import org.example.core.models.ComputeResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DockerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerManager.class);

    private final SshClient sshClient;

    public DockerManager(SshClient sshClient) {
        this.sshClient = sshClient;
    }

    public void startContainer(String containerName,
                               String volumePath,
                               String dockerfilePath,
                               ComputeResource computeResource
                               ) {
        var buildCommand = "cd " + dockerfilePath + " && docker build -t " + containerName + " .";
        var buildResult = sshClient.executeCommand(buildCommand);

        if(buildResult.getExitCode() != 0){
            throw new RuntimeException("Container not built");
        }

        var runCommand = GetRunDockerCommand(containerName, volumePath, computeResource);

        var runResult = sshClient.executeCommand(runCommand);

        if(runResult.getExitCode() != 0){
            throw new RuntimeException("Container not started");
        }

        LOGGER.info("Container " + containerName + " ran.");
    }

    private static String GetRunDockerCommand(String containerName, String volumePath, ComputeResource computeResource) {
        int cpuCores = computeResource.getCpuCores();
        int ramMB = computeResource.getRam(); // ОЗУ в мегабайтах

        // 3. Формируем команду запуска контейнера с ограничениями
        var runCommand = String.format(
                "docker run -d --rm " +
                        "--cpus=%d " +       // Ограничение CPU
                        "--memory=%dm " +    // Ограничение RAM (мегабайты)
                        "--name %s " +
                        "-v %s:/app %s",
                cpuCores, ramMB, containerName, volumePath, containerName
        );

        return runCommand;
    }

    public CompletableFuture<Void> waitForCompletion(String containerName) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable checkContainer = new Runnable() {
            @Override
            public void run() {
                if (future.isCancelled()) {
                    var result = sshClient.executeCommand("docker kill " + containerName);

                    if(!result.isSuccess()){
                        LOGGER.error("Контейнер " + containerName + " не завершил работу принудительно");
                        LOGGER.error(result.getStdout() + "\n" + result.getStderr());
                    }

                    scheduler.shutdownNow();
                    return;
                }

                try {
                    var runningContainers = sshClient.executeCommand("docker ps -q -f name=" + containerName);

                    if (runningContainers.getStdout().trim().isEmpty()) {
                        LOGGER.info("Контейнер " + containerName + " завершил работу.");
                        future.complete(null);
                    } else {
                        scheduler.schedule(this, 5, TimeUnit.SECONDS); // Повторяем через 5 секунд
                    }
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }
        };

        scheduler.schedule(checkContainer, 0, TimeUnit.SECONDS);
        return future;
    }
}
