package org.example.server.clients;

import org.example.server.configurations.VBoxConfig;
import org.example.server.models.VirtualMachineState;
import org.example.server.models.commands.host_executor.ICommandExecutor;
import org.example.server.models.commands.vbox_manage.VBoxManageCommandBuilder;
import org.example.server.services.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

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

    public boolean createVirtualMachineIfNotExist(String virtualMachineName) {
        if (virtualMachineIsExist((virtualMachineName))) {
            return false;
        }

        var resource = new ClassPathResource(vBoxConfig.defaultImagePath);

        var commandBuilder = VBoxManageCommandBuilder.create();
        var command = commandBuilder
                .executable(settingService.getVirtualBoxPath())
                .createFromOVA(resource.getPath(), virtualMachineName)
                .toString();

        try {
            commandExecutor.executeCommand(command);
        } catch (Exception e) {
            throw new RuntimeException();
        }

        return true;
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

    public void addSharedFolderToVirtualMachine(String virtualMachineName) {
        var states = List.of(VirtualMachineState.POWEROFF);

        if (!virtualMachineCheckState(virtualMachineName, states)) {
            throw new IllegalStateException("Невозможно монтировать папку в уже запущенную машину");
        }

        var commandBuilder = VBoxManageCommandBuilder.create();

        var command = commandBuilder
                .executable(settingService.getVirtualBoxPath())
                .addSharedFolder(vBoxConfig.sharedFolder, virtualMachineName)
                .toString();
        try {
            commandExecutor.executeCommand(command);
        } catch (Exception e) {
            throw new RuntimeException();
        }
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

    public String getVirtualMachineIp(String name) {
        if (!virtualMachineCheckState(name, List.of(VirtualMachineState.RUNNING))) {
            throw new IllegalStateException("Машина не запущена, невозможно получить IP");
        }

        var commandBuilder = VBoxManageCommandBuilder.create();
        var command = commandBuilder
                .executable(settingService.getVirtualBoxPath())
                .guestPropertyEnumerate(name)
                .toString();

        ArrayList<String> result;

        try {
            result = commandExecutor.executeCommand(command).get(0);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при выполнении команды VBoxManage", e);
        }

        var ip = parseIpFromCommandOutput(result);

        if (ip == null) {
            throw new RuntimeException("Не удалось найти IP-адрес для машины " + name);
        }

        return ip;
    }

    private String parseIpFromCommandOutput(List<String> output) {
        var ipPattern = ".*?=\\s*'((\\d{1,3}\\.){3}\\d{1,3})'.*";

        for (String line : output) {
            if (!line.contains("/VirtualBox/GuestInfo/Net/0/V4/IP")) {
                continue;
            }

            var pattern = Pattern.compile(ipPattern);
            var matcher = pattern.matcher(line);

            if (matcher.matches()) {
                String ip = matcher.group(1);
                System.out.println("Найден IP-адрес: " + ip);
                return ip;
            }
            System.out.println("IP-адрес не найден в строке.");
        }

        return null;
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
