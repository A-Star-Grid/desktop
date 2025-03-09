package org.example;

public class ResetSettingsCommand implements ConsoleCommand {
    private final ServerClient client = new ServerClient();

    @Override
    public void setArgs(String[] args) {
        // Нет аргументов, просто вызываем API
    }

    @Override
    public void execute() {
        String response = client.resetSettings();
        System.out.println(response);
    }
}
