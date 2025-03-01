package org.example.clients;

import org.example.configurations.VBoxConfig;
import org.example.models.commands.executor.ICommandExecutor;
import org.example.models.commands.vbox_manage.VBoxManageCommandBuilder;
import org.example.services.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

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

    private boolean virtualMachineIsExist(String name) {
        var commandBuilder = VBoxManageCommandBuilder.create();
        var command = commandBuilder.executable(settingService.getVirtualBoxPath()).list().toString();
        ArrayList<String> result;

        try {
            result = commandExecutor.executeCommand(command).get(0);
        } catch (Exception e) {
            throw new RuntimeException();
        }

        return result.stream().anyMatch(m -> m.contains(name));
    }
}
