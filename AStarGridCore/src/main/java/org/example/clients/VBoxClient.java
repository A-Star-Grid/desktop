package org.example.clients;

import org.example.configurations.VBoxConfig;
import org.example.models.VirtualMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;

@Component
public class VBoxClient {
    private final VBoxConfig vBoxConfig;

    @Autowired
    public VBoxClient(VBoxConfig vBoxConfig) {
        this.vBoxConfig = vBoxConfig;
    }

    public String createVirtualMachine(String virtualMachineName) {
        var fullPath = Paths.get(vBoxConfig.vboxPath, "VBoxManage").toString();

        var command = String.format(
                "\"\"%s\" import %s --vsys 0 --vmname \"%s\"\"",
                fullPath,
                vBoxConfig.defaultImagePath,
                virtualMachineName
        );

        executeCommand(command);

        return virtualMachineName;
    }

    public void getVirtualMachines() {
        String fullPath = Paths.get(vBoxConfig.vboxPath, "VBoxManage").toString();
        String command = String.format("%s list vms", fullPath);
        executeCommand(command);
    }

    public VirtualMachine startVirtualMachine(String name) {
        String fullPath = Paths.get(vBoxConfig.vboxPath, "VBoxManage").toString();
        String command = String.format("%s startvm \"%s\" --type headless", fullPath, name);
        executeCommand(command);
        System.out.println("Virtual Machine started: " + name);
        return new VirtualMachine();
    }

    public String getVirtualMachineIp(String name) {
        String fullPath = Paths.get(vBoxConfig.vboxPath, "VBoxManage").toString();
        String command = String.format("%s guestproperty enumerate \"%s\"", fullPath, name);
        String ipAddress = null;

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                processBuilder = new ProcessBuilder("cmd", "/c", command);
            }

            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("V4/IP")) { // Ищем строку с IP-адресом
                        ipAddress = line.split(",")[1].trim().split(":")[1].trim();
                        break;
                    }
                }
            }

            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (ipAddress == null) {
            throw new RuntimeException("Unable to retrieve IP address for VM: " + name);
        }

        System.out.println("IP Address for VM " + name + ": " + ipAddress);
        return ipAddress;
    }

    public static void executeCommand(String command) {
        try {
            var os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder processBuilder;

            if (os.contains("win")) {
                processBuilder = new ProcessBuilder("cmd", "/c", command);
            } else {
                processBuilder = new ProcessBuilder("bash", "-c", command);
            }

            var e = processBuilder.command();
            Process process = processBuilder.start();

            // Чтение стандартного вывода
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                System.out.println("Command Output:");
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            // Чтение ошибок
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                System.out.println("Error Output:");
                while ((line = errorReader.readLine()) != null) {
                    System.err.println(line);
                }
            }

            // Ожидание завершения процесса
            int exitCode = process.waitFor();
            System.out.println("Exit Code: " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
