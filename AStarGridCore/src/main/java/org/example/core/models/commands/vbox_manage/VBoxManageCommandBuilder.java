package org.example.core.models.commands.vbox_manage;

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

    public VBoxManageCommandBuilder addCommandEdge(){
        var osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            commandParts.add("&");
        } else {
            commandParts.add(";");
        }

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

    public VBoxManageCommandBuilder addSharedFolder(String path, String folderName, String vmName){
        commandParts.add("sharedfolder add");
        commandParts.add(escapeArgument(vmName));
        commandParts.add("--name");
        commandParts.add(" \"" + folderName + "\" ");
        commandParts.add("--hostpath " + escapeArgument(path));
        commandParts.add("--automount");
        return this;
    }

    public VBoxManageCommandBuilder getInfo(String vmName) {
        commandParts.add("showvminfo");
        commandParts.add(escapeArgument(vmName));
        commandParts.add("--machinereadable");
        return this;
    }

    public VBoxManageCommandBuilder start(String vmName) {
        commandParts.add("startvm");
        commandParts.add(escapeArgument(vmName));
        commandParts.add("--type headless");
        return this;
    }

    public VBoxManageCommandBuilder guestPropertyEnumerate(String vmName) {
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
