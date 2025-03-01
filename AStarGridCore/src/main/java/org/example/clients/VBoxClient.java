package org.example.clients;

import org.example.configurations.VBoxConfig;
import org.example.models.VirtualMachineState;
import org.example.models.commands.host_executor.ICommandExecutor;
import org.example.models.commands.vbox_manage.VBoxManageCommandBuilder;
import org.example.services.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class VBoxClient {
    private final VBoxConfig vBoxConfig;
    private final SettingService settingService;
    private final ICommandExecutor commandExecutor;

    @Autowired
    public VBoxClient(VBoxConfig vBoxConfig,
                      ICommandExecutor commandExecutor,
                      SettingService settingService) {
        this.vBoxConfig = vBoxConfig;
        this.settingService = settingService;
        this.commandExecutor = commandExecutor;
    }

    public void createVirtualMachineIfNotExist(String virtualMachineName) {
        if (virtualMachineIsExist((virtualMachineName))) {
            return;
        }

        var commandBuilder = VBoxManageCommandBuilder.create();

        var resource = new ClassPathResource(vBoxConfig.defaultImagePath);

        var command = commandBuilder
                .executable(settingService.getVirtualBoxPath())
                .createFromOVA(resource.getPath(), virtualMachineName)
                .toString();

        try {
            commandExecutor.executeCommand(command);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public void startVirtualMachineIfNotRunning(String virtualMachineName) {
        var states = List.of(VirtualMachineState.RUNNING, VirtualMachineState.STARTING);

        if (virtualMachineCheckState(virtualMachineName, states)) {
            return;
        }

        var commandBuilder = VBoxManageCommandBuilder.create();

        var command = commandBuilder
                .executable(settingService.getVirtualBoxPath())
                .start(virtualMachineName)
                .toString();

        try {
            commandExecutor.executeCommand(command);
        } catch (Exception e) {
            throw new RuntimeException();
        }

        waitForVirtualMachineState(virtualMachineName, VirtualMachineState.RUNNING, 60);
    }

    public void stopVirtualMachine(String virtualMachineName) {
        if (virtualMachineCheckState(virtualMachineName, List.of(VirtualMachineState.POWEROFF))) {
            return;
        }

        var commandBuilder = VBoxManageCommandBuilder.create();

        var command = commandBuilder
                .executable(settingService.getVirtualBoxPath())
                .poweroff(virtualMachineName)
                .toString();

        try {
            commandExecutor.executeCommand(command);
        } catch (Exception e) {
            throw new RuntimeException();
        }

        waitForVirtualMachineState(virtualMachineName, VirtualMachineState.POWEROFF, 60);
    }

    private boolean virtualMachineIsExist(String name) {
        var commandBuilder = VBoxManageCommandBuilder.create();
        var command = commandBuilder
                .executable(settingService.getVirtualBoxPath())
                .list()
                .toString();
        ArrayList<String> result;

        try {
            result = commandExecutor.executeCommand(command).get(0);
        } catch (Exception e) {
            throw new RuntimeException();
        }

        return result.stream().anyMatch(m -> m.contains(name));
    }

    private boolean virtualMachineCheckState(String name, List<VirtualMachineState> states) {
        var commandBuilder = VBoxManageCommandBuilder.create();
        var command = commandBuilder
                .executable(settingService.getVirtualBoxPath())
                .getInfo(name)
                .toString();

        ArrayList<String> result;

        try {
            result = commandExecutor.executeCommand(command).get(0);
        } catch (Exception e) {
            throw new RuntimeException();
        }
        var state = result.stream().filter(l -> l.contains("VMState=")).findFirst().get();

        return states.stream().anyMatch(s -> state.contains(s.getStateName()));
    }

    private void waitForVirtualMachineState(String virtualMachineName,
                                            VirtualMachineState expectedState,
                                            int timeoutSeconds) {
        var scheduler = Executors.newScheduledThreadPool(1);

        var future = new CompletableFuture<>();

        var scheduledTask = scheduler.scheduleAtFixedRate(() -> {
            if (virtualMachineCheckState(virtualMachineName, List.of(expectedState))) {
                future.complete(null); // Завершаем future, если ВМ в нужном состоянии
            }
        }, 0, 2, TimeUnit.SECONDS); // Проверяем каждую 2 секунды

        try {
            future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException("Таймаут: Виртуальная машина не запустилась за " + timeoutSeconds + " секунд");
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при ожидании запуска виртуальной машины", e);
        } finally {
            scheduledTask.cancel(true);
            scheduler.shutdown();
        }
    }

}
