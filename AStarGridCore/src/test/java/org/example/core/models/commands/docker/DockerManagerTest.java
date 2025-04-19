package org.example.core.models.commands.docker;

import org.example.core.clients.SshClient;
import org.example.core.models.ComputeResource;
import org.example.core.models.commands.CommandResult;
import org.example.core.models.commands.docker.DockerManager;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DockerManagerTest {

    @Mock SshClient sshClient;
    DockerManager dockerManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dockerManager = new DockerManager(sshClient);
    }

    @Test
    @DisplayName("startContainer: успешный билд и запуск")
    void testStartContainer_success() {
        when(sshClient.executeCommand(contains("docker build"))).thenReturn(success());
        when(sshClient.executeCommand(contains("docker run"))).thenReturn(success());

        assertDoesNotThrow(() -> dockerManager.startContainer(
                "test-container", "/path", "/docker", new ComputeResource(1, 1000, 512)
        ));

        verify(sshClient).executeCommand(contains("docker build"));
        verify(sshClient).executeCommand(contains("docker run"));
    }

    @Test
    @DisplayName("startContainer: ошибка сборки контейнера")
    void testStartContainer_buildFails() {
        when(sshClient.executeCommand(contains("docker build"))).thenReturn(fail());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                dockerManager.startContainer("fail-container", "/path", "/docker", new ComputeResource(1, 1000, 512)));

        assertTrue(ex.getMessage().contains("Container not built"));
    }

    @Test
    @DisplayName("startContainer: ошибка запуска контейнера")
    void testStartContainer_runFails() {
        when(sshClient.executeCommand(contains("docker build"))).thenReturn(success());
        when(sshClient.executeCommand(contains("docker run"))).thenReturn(fail());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                dockerManager.startContainer("fail-container", "/path", "/docker", new ComputeResource(1, 1000, 512)));

        assertTrue(ex.getMessage().contains("Container not started"));
    }

    @Test
    @DisplayName("waitForCompletion: контейнер завершён сразу")
    void testWaitForCompletion_doneImmediately() throws Exception {
        when(sshClient.executeCommand(contains("docker ps"))).thenReturn(new CommandResult("", "", 0));

        CompletableFuture<Void> future = dockerManager.waitForCompletion("test-container");

        assertDoesNotThrow(() -> future.get(2, TimeUnit.SECONDS));
        verify(sshClient).executeCommand("docker ps -q -f name=test-container");
    }

    @Test
    @DisplayName("waitForCompletion: отмена завершает future и убивает контейнер")
    void testWaitForCompletion_cancellationKillsContainer() throws Exception {
        when(sshClient.executeCommand(contains("docker ps"))).thenReturn(new CommandResult("123abc", "", 0));
        when(sshClient.executeCommand(contains("docker kill"))).thenReturn(success());

        CompletableFuture<Void> future = dockerManager.waitForCompletion("some-container");
        future.cancel(true);

        Thread.sleep(1000); // даём немного времени потоку завершиться

        assertTrue(future.isCancelled() || future.isDone());
        verify(sshClient).executeCommand("docker kill some-container");
    }

    @Test
    @DisplayName("GetRunDockerCommand: формирует правильную команду")
    void testGetRunDockerCommand() {
        var compute = new ComputeResource(2, 2048, 1024);
        String cmd = invokeRunCommand("container", "/mnt/vol", compute);

        assertTrue(cmd.contains("--cpus=2"));
        assertTrue(cmd.contains("--memory=1024m"));
        assertTrue(cmd.contains("-v /mnt/vol:/app"));
        assertTrue(cmd.endsWith("container"));
    }

    private CommandResult success() {
        return new CommandResult("OK", "", 0);
    }

    private CommandResult fail() {
        return new CommandResult("ERR", "fail", 1);
    }

    private String invokeRunCommand(String name, String path, ComputeResource res) {
        try {
            var m = DockerManager.class.getDeclaredMethod("GetRunDockerCommand", String.class, String.class, ComputeResource.class);
            m.setAccessible(true);
            return (String) m.invoke(null, name, path, res);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
