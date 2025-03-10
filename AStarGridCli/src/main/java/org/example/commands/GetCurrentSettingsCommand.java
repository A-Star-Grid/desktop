package org.example.commands;

import org.example.ServerClient;

public class GetCurrentSettingsCommand implements ConsoleCommand {
    private final ServerClient client = new ServerClient();

    @Override
    public void setArgs(String[] args) {
        // Нет аргументов, просто вызываем API
    }

    @Override
    public void execute() {
        String subscribes = client.getCurrentSettings();
        System.out.println("Settings:");
        System.out.println(subscribes);
    }
}
