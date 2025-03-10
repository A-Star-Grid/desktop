package org.example.commands;


import org.example.ServerClient;

public class GetSubscribesCommand implements ConsoleCommand {
    private final ServerClient client = new ServerClient();

    @Override
    public void setArgs(String[] args) {
        // Нет аргументов, просто вызываем API
    }

    @Override
    public void execute() {
        String subscribes = client.getSubscribes();
        System.out.println("Subscribes:");
        System.out.println(subscribes);
    }
}
