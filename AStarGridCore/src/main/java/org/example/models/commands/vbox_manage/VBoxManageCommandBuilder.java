package org.example.models.commands.vbox_manage;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class VBoxManageCommandBuilder {
    private final List<String> commandParts = new ArrayList<>();
    private String executablePath = "VBoxManage";

    private VBoxManageCommandBuilder() {
    }

    public static VBoxManageCommandBuilder create() {
        return new VBoxManageCommandBuilder();
    }

    public VBoxManageCommandBuilder executable(String path) {
        var fullPath = Paths.get(path, "VBoxManage").toString();
        this.executablePath = escapeArgument(fullPath);
        return this;
    }

    public VBoxManageCommandBuilder createFromOVA(String ovaPath, String vmName) {
        commandParts.add("import");
        commandParts.add(escapeArgument(ovaPath));
        commandParts.add("--vsys");
        commandParts.add("0");
        commandParts.add("--vmname");
        commandParts.add(escapeArgument(vmName));
        return this;
    }

    public VBoxManageCommandBuilder startWithSharedFolder(String vmName, String sharedFolderName, String hostPath) {
        commandParts.add("startvm");
        commandParts.add(escapeArgument(vmName));
        commandParts.add("--shared-folder");
        commandParts.add(String.format("%s,%s",
                escapeArgument(hostPath),
                escapeArgument(sharedFolderName)));
        return this;
    }

    public VBoxManageCommandBuilder guestpropertyEnumerate(String vmName) {
        commandParts.add("guestproperty");
        commandParts.add("enumerate");
        commandParts.add(escapeArgument(vmName));
        return this;
    }

    public VBoxManageCommandBuilder poweroff(String vmName) {
        commandParts.add("controlvm");
        commandParts.add(escapeArgument(vmName));
        commandParts.add("poweroff");
        return this;
    }

    public VBoxManageCommandBuilder list() {
        commandParts.add("list vms");
        return this;
    }

    public List<String> build() {
        List<String> fullCommand = new ArrayList<>();
        fullCommand.add(executablePath);
        fullCommand.addAll(commandParts);
        return fullCommand;
    }

    private String escapeArgument(String arg) {
        if (arg.contains(" ")) {
            return "\"" + arg + "\"";
        }
        return arg;
    }

    @Override
    public String toString() {
        return String.join(" ", build());
    }
}
