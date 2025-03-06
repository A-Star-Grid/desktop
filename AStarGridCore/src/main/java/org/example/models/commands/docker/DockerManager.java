package org.example.models.commands.docker;

import org.example.clients.SshClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DockerManager {
    private final SshClient sshClient;

    public DockerManager(SshClient sshClient) {
        this.sshClient = sshClient;
    }

    public void startContainer(String containerName, String volumePath, String dockerfilePath) throws Exception {
        var buildCommand = "cd " + dockerfilePath + " && docker build -t " + containerName + " .";
        var buildResult = sshClient.executeCommand(buildCommand);

        if(buildResult.getExitCode() != 0){
            throw new RuntimeException("Container not builded");
        }

        var runCommand = "docker run -d --rm -v " + volumePath + ":/app " + containerName;
        var runResult = sshClient.executeCommand(runCommand);

        if(runResult.getExitCode() != 0){
            throw new RuntimeException("Container not started");
        }

        System.out.println("✅ Контейнер " + containerName + " запущен.");
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
