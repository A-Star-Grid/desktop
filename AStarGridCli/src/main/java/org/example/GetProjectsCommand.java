package org.example;


public class GetProjectsCommand implements ConsoleCommand {
    private int page = 1;
    private int perPage = 5;
    private final ServerClient client = new ServerClient();

    @Override
    public void setArgs(String[] args) {
        for (int i = 1; i < args.length; i++) {
            if ("--page".equals(args[i]) && i + 1 < args.length) {
                page = Integer.parseInt(args[i + 1]);
            }
            if ("--perPage".equals(args[i]) && i + 1 < args.length) {
                perPage = Integer.parseInt(args[i + 1]);
            }
        }
    }

    @Override
    public void execute() {
        String projects = client.getProjects(page, perPage);
        System.out.println("Список проектов:");
        System.out.println(projects);
    }
}
