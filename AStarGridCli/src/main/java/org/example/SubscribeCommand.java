package org.example;

public class SubscribeCommand implements ConsoleCommand {
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
            System.out.println("Ошибка: укажите ID проекта с параметром --projectId");
            return;
        }

        client.subscribeToProject(projectId);
        System.out.println("Вы подписались на проект " + projectId);
    }
}
