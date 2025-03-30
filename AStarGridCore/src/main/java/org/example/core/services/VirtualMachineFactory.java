package org.example.core.services;

import org.example.core.configurations.AppSettings;
import org.example.core.configurations.VBoxConfig;
import org.example.core.models.VirtualMachineState;
import org.example.core.models.commands.CommandResult;
import org.example.core.models.commands.host_executor.ICommandExecutor;
import org.example.core.models.commands.vbox_manage.VBoxManageCommandBuilder;
import org.example.core.services.settings.ApplicationSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class VirtualMachineFactory {
    private final VBoxConfig vBoxConfig;
    private final ApplicationSettingsService applicationSettingsService;
    private final ICommandExecutor commandExecutor;
    private final AppSettings appSettings;


    private static Optional<VirtualMachine> instance;
    private static final ReentrantReadWriteLock staticLock = new ReentrantReadWriteLock(); // Lock for singleton creation
    private static String virtualMachineName;

    @Autowired
    public VirtualMachineFactory(VBoxConfig vBoxConfig,
                                 ICommandExecutor commandExecutor,
                                 ApplicationSettingsService applicationSettingsService,
                                 PreferencesStorage preferencesStorage,
                                 AppSettings appSettings) {
        this.vBoxConfig = vBoxConfig;
        this.applicationSettingsService = applicationSettingsService;
        this.commandExecutor = commandExecutor;
        this.appSettings = appSettings;
        virtualMachineName = "vm-" + preferencesStorage.getDeviceUUID().toString();
        instance = Optional.empty();
    }

    public VirtualMachine createVirtualMachineIfNotExist() {
        staticLock.writeLock().lock();

        try {
            if (!virtualMachineIsExist((virtualMachineName))) {
                var resource = new ClassPathResource(vBoxConfig.getDefaultImagePath());

                var commandBuilder = VBoxManageCommandBuilder.create();
                var command = commandBuilder
                        .executable(applicationSettingsService.getVirtualBoxPath())
                        .createFromOVA(resource.getPath(), virtualMachineName)
                        .toString();

                try {
                    commandExecutor.executeCommand(command);
                } catch (Exception e) {
                    throw new RuntimeException();
                }

                addSharedFolderToVirtualMachine(virtualMachineName);
            }

            if(getVirtualMachine().isEmpty()){
                throw new RuntimeException("Virtual Machine not ran");
            }

            return getVirtualMachine().get();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Error of get info about vm");
        } finally {
            staticLock.writeLock().unlock();
        }
    }

    public Optional<VirtualMachine> getVirtualMachine() {
        var vmOptional = getVirtualMachineInfoFromHost();

        if(vmOptional.isEmpty()){
            return Optional.empty();
        }

        var vm = vmOptional.get();

        if (instance.isEmpty()) {
            instance = Optional.of(vm);
        } else {

            instance.get().setCpu(vm.getCpu());
            instance.get().setDisk(vm.getDisk());
            instance.get().setName(vm.getName());
            instance.get().setIp(vm.getIp());
            instance.get().setSharedFolderPaths(vm.getSharedFolderPath());
            instance.get().setRam(vm.getRam());
            instance.get().setVirtualMachineState(vm.getVirtualMachineState());
        }

        return instance;
    }

    public Optional<VirtualMachine> setVirtualMachineCpu(Integer cpu) {
        var vmOptional = getVirtualMachineInfoFromHost();

        if (vmOptional.isEmpty() || vmOptional.get().virtualMachineState != VirtualMachineState.POWEROFF) {
           throw new IllegalStateException("Невозможно изменить CPU пока виртуальная машина запущена или не создана");
        }

        var commandBuilder = VBoxManageCommandBuilder.create();

        var command = commandBuilder
                .executable(applicationSettingsService.getVirtualBoxPath())
                .setCpu(virtualMachineName, cpu)
                .toString();

        CommandResult commandResult;

        try {
            commandResult = commandExecutor.executeCommand(command);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось изменить количество CPU");
        }

        if (!commandResult.isSuccess()){
            throw new RuntimeException("Не удалось изменить количество CPU");
        }

        vmOptional.get().setCpu(cpu);

        return vmOptional;
    }

    public Optional<VirtualMachine> setVirtualMachineRam(Integer ram) {
        var vmOptional = getVirtualMachineInfoFromHost();

        if (vmOptional.isEmpty() || vmOptional.get().virtualMachineState != VirtualMachineState.POWEROFF) {
            throw new IllegalStateException("Невозможно изменить RAM пока виртуальная машина запущена или не создана");
        }

        var commandBuilder = VBoxManageCommandBuilder.create();

        var command = commandBuilder
                .executable(applicationSettingsService.getVirtualBoxPath())
                .setRam(virtualMachineName, ram)
                .toString();

        CommandResult commandResult;

        try {
            commandResult = commandExecutor.executeCommand(command);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось изменить количество RAM");
        }

        if (!commandResult.isSuccess()){
            throw new RuntimeException("Не удалось изменить количество RAM");
        }

        vmOptional.get().setRam(ram);

        return vmOptional;
    }

    public void stopVirtualMachine() {
        if(getVirtualMachine().isEmpty()){
            return;
        }

        if (virtualMachineCheckState(virtualMachineName, List.of(VirtualMachineState.POWEROFF))) {
            return;
        }

        var commandBuilder = VBoxManageCommandBuilder.create();

        var command = commandBuilder
                .executable(applicationSettingsService.getVirtualBoxPath())
                .poweroff(virtualMachineName)
                .toString();

        try {
            commandExecutor.executeCommand(command);
        } catch (Exception e) {
            throw new RuntimeException();
        }

        waitForVirtualMachineState(virtualMachineName, VirtualMachineState.POWEROFF, 60);
    }


    public void startVirtualMachineIfNotRunning() {
        var states = List.of(VirtualMachineState.RUNNING, VirtualMachineState.STARTING);

        if (virtualMachineCheckState(virtualMachineName, states)) {
            return;
        }

        var commandBuilder = VBoxManageCommandBuilder.create();

        var command = commandBuilder
                .executable(applicationSettingsService.getVirtualBoxPath())
                .start(virtualMachineName)
                .toString();

        try {
            commandExecutor.executeCommand(command);
        } catch (Exception e) {
            throw new RuntimeException();
        }

        waitForVirtualMachineState(virtualMachineName, VirtualMachineState.RUNNING, 60);
    }

    private void addSharedFolderToVirtualMachine(String virtualMachineName) throws IOException {
        var states = List.of(VirtualMachineState.POWEROFF);

        if (!virtualMachineCheckState(virtualMachineName, states)) {
            throw new IllegalStateException("Невозможно монтировать папку в уже запущенную машину");
        }

        var commandBuilder = VBoxManageCommandBuilder.create();

        if (!Files.exists(Path.of(appSettings.taskArchivesDirectory))) {
            Files.createDirectories(Path.of(appSettings.taskArchivesDirectory));
        }

        var command = commandBuilder
                .executable(applicationSettingsService.getVirtualBoxPath())
                .addSharedFolder(appSettings.taskArchivesDirectory, vBoxConfig.sharedFolder, virtualMachineName)
                .toString();
        try {
            commandExecutor.executeCommand(command);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private Optional<VirtualMachine> getVirtualMachineInfoFromHost() {
        var commandBuilder = VBoxManageCommandBuilder.create();
        var command = commandBuilder
                .executable(applicationSettingsService.getVirtualBoxPath())
                .getInfo(virtualMachineName)
                .toString();

        CommandResult rawInfo;

        try {
            rawInfo = commandExecutor.executeCommand(command);
        } catch (Exception ex) {
            throw new RuntimeException("error of get info about VM");
        }

        if (rawInfo.isSuccess()) {
            return Optional.of(parseVirtualMachineInfo(rawInfo.getListStdout()));
        }

        return Optional.empty();
    }

    private boolean virtualMachineIsExist(String name) {
        var commandBuilder = VBoxManageCommandBuilder.create();
        var command = commandBuilder
                .executable(applicationSettingsService.getVirtualBoxPath())
                .list()
                .toString();
        CommandResult result;

        try {
            result = commandExecutor.executeCommand(command);
        } catch (Exception e) {
            throw new RuntimeException();
        }

        return result.getListStdout().stream().anyMatch(m -> m.contains(name));
    }

    private boolean virtualMachineCheckState(String name, List<VirtualMachineState> states) {
        var commandBuilder = VBoxManageCommandBuilder.create();
        var command = commandBuilder
                .executable(applicationSettingsService.getVirtualBoxPath())
                .getInfo(name)
                .toString();

        CommandResult result;

        try {
            result = commandExecutor.executeCommand(command);
        } catch (Exception e) {
            throw new RuntimeException();
        }
        var state = result.getListStdout().stream().filter(l -> l.contains("VMState=")).findFirst().get();

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

    private VirtualMachine parseVirtualMachineInfo(List<String> lines) {
        String name = null;
        double cpu = 0;
        int ram = 0;
        int disk = 0;
        var sharedFolderPaths = new ArrayList<String>();
        var state = VirtualMachineState.UNKNOWN;

        for (var line : lines) {
            var parts = line.split("=", 2);
            if (parts.length != 2) continue;

            var key = parts[0].trim();
            var value = parts[1].replace("\"", "").trim();

            switch (key) {
                case "name":
                    name = value;
                    break;
                case "cpus":
                    cpu = Double.parseDouble(value);
                    break;
                case "memory":
                    ram = Integer.parseInt(value);
                    break;
                case "SATA-0-0":
                    disk = getDiskSize(value);
                    break;
                case "VMState":
                    state = VirtualMachineState.fromStateName(value);
                    break;
                default:
                    if (key.startsWith("SharedFolderPathMachineMapping")) {
                        sharedFolderPaths.add(value);
                    }
                    break;
            }
        }

        return new VirtualMachine("localhost", name, cpu, ram, disk, sharedFolderPaths, state);
    }

    private static int getDiskSize(String path) {
        // TODO Realize getting dick space
        return 1024;
    }

    public class VirtualMachine {
        private String ip;
        private String name;
        private double cpu;
        private int ram;
        private int disk;
        private List<String> sharedFolderPaths;
        private VirtualMachineState virtualMachineState;

        public VirtualMachine(
                String ip,
                String name,
                double cpu,
                int ram,
                int disk,
                List<String> sharedFolderPaths,
                VirtualMachineState virtualMachineState) {
            this.ip = ip;
            this.name = name;
            this.cpu = cpu;
            this.ram = ram;
            this.disk = disk;
            this.sharedFolderPaths = sharedFolderPaths;
            this.virtualMachineState = virtualMachineState;
        }

        public String getIp() {
            return ip;
        }

        public String getName() {
            return name;
        }

        public double getCpu() {
            return cpu;
        }

        public int getRam() {
            return ram;
        }

        public int getDisk() {
            return disk;
        }

        public List<String> getSharedFolderPath() {
            return sharedFolderPaths;
        }

        public VirtualMachineState getVirtualMachineState() {
            return virtualMachineState;
        }

        private void setIp(String ip) {
            this.ip = ip;
        }

        private void setName(String name) {
            this.name = name;
        }

        private void setCpu(double cpu) {
            this.cpu = cpu;
        }

        private void setRam(int ram) {
            this.ram = ram;
        }

        private void setDisk(int disk) {
            this.disk = disk;
        }

        private void setSharedFolderPaths(List<String> sharedFolderPaths) {
            this.sharedFolderPaths = sharedFolderPaths;
        }

        private void setVirtualMachineState(VirtualMachineState virtualMachineState) {
            this.virtualMachineState = virtualMachineState;
        }
    }
}
