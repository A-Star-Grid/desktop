package org.example.cli;

import org.example.cli.commands.*;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {
    private final Map<String, ConsoleCommand> commands = new HashMap<>();

    public CommandFactory() {
        commands.put("login", new LoginCommand());
        commands.put("list-projects", new GetProjectsCommand());
        commands.put("set-ram", new SetRamCommand());
        commands.put("set-cpu", new SetCpuCommand());
        commands.put("set-disk", new SetDiskCommand());
        commands.put("activate-computation", new ActivateComputationCommand());
        commands.put("reset-settings", new ResetSettingsCommand());
        commands.put("subscribe", new SubscribeCommand());
        commands.put("unsubscribe", new UnsubscribeCommand());
        commands.put("get-subscribes", new GetSubscribesCommand());
        commands.put("get-settings", new GetCurrentSettingsCommand());
    }

    public Runnable getCommand(String[] args) {
        if (args.length == 0) {
            return null;
        }

        ConsoleCommand command = commands.get(args[0]);
        if (command != null) {
            command.setArgs(args);
        }

        return command;
    }
}
