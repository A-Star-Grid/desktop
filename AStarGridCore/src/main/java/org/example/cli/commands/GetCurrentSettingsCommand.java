package org.example.cli.commands;

import org.example.cli.ServerClient;

public class GetCurrentSettingsCommand implements ConsoleCommand {
    private final ServerClient client;

    public GetCurrentSettingsCommand(){
        client = new ServerClient();
    }

    public GetCurrentSettingsCommand(ServerClient client){
        this.client = client;
    }

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
