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
        // 1. Подготавливаем контейнер
        String buildCommand = "cd " + dockerfilePath + " && docker build -t " + containerName + " .";
        sshClient.executeCommand(buildCommand);

        // 2. Запускаем контейнер
        String runCommand = "docker run --rm -v -d" + volumePath + ":/app/ " + containerName;
        sshClient.executeCommand(runCommand);

        System.out.println("✅ Контейнер " + containerName + " запущен.");
    }

    public CompletableFuture<Void> waitForCompletion(String containerName) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable checkContainer = new Runnable() {
            @Override
            public void run() {
                try {
                    String runningContainers = sshClient.executeCommand("docker ps -q -f name=" + containerName);

                    if (runningContainers.trim().isEmpty()) {
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
