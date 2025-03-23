package org.example.core.models.commands.docker;

import org.example.core.clients.SshClient;
import org.example.core.models.ComputeResource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DockerManager {
    private final SshClient sshClient;

    public DockerManager(SshClient sshClient) {
        this.sshClient = sshClient;
    }

    public void startContainer(String containerName,
                               String volumePath,
                               String dockerfilePath,
                               ComputeResource computeResource
                               ) throws Exception {
        var buildCommand = "cd " + dockerfilePath + " && docker build -t " + containerName + " .";
        var buildResult = sshClient.executeCommand(buildCommand);

        if(buildResult.getExitCode() != 0){
            throw new RuntimeException("Container not built");
        }

        var runCommand = GetBuildDockerCommand(containerName, volumePath, computeResource);

        var runResult = sshClient.executeCommand(runCommand);

        if(runResult.getExitCode() != 0){
            throw new RuntimeException("Container not started");
        }

        System.out.println("Контейнер " + containerName + " запущен.");
    }

    private static String GetBuildDockerCommand(String containerName, String volumePath, ComputeResource computeResource) {
        int cpuCores = computeResource.getCpuCores();
        int ramMB = computeResource.getRam(); // ОЗУ в мегабайтах

        // 3. Формируем команду запуска контейнера с ограничениями
        var runCommand = String.format(
                "docker run -d --rm " +
                        "--cpus=%d " +       // Ограничение CPU
                        "--memory=%dm " +    // Ограничение RAM (мегабайты)
                        "-v %s:/app %s",
                cpuCores, ramMB, volumePath, containerName
        );

        return runCommand;
    }

    public CompletableFuture<Void> waitForCompletion(String containerName) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable checkContainer = new Runnable() {
            @Override
            public void run() {
                try {
                    var runningContainers = sshClient.executeCommand("docker ps -q -f name=" + containerName);

                    if (runningContainers.getStdout().trim().isEmpty()) {
                        System.out.println("✅ Контейнер " + containerName + " завершил работу.");
                        future.complete(null); // Завершаем `CompletableFuture`
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
