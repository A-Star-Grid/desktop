package org.example.cli.commands;

import org.example.cli.ServerClient;

public class UnsubscribeCommand implements ConsoleCommand {
    private int projectId;
    private final ServerClient client = new ServerClient();

    @Override
    public void setArgs(String[] args) {
        for (int i = 1; i < args.length; i++) {
            if ("--projectId".equals(args[i]) && i + 1 < args.length) {
                projectId = Integer.parseInt(args[i + 1]);
            }
        }
    }

    @Override
    public void execute() {
        if (projectId <= 0) {
            System.out.println("Error: need --projectId");
            return;
        }

        client.unsubscribeFromProject(projectId);
        System.out.println("Success unsubscribe " + projectId);
    }
}
