package org.example.cli.commands;


import org.example.cli.ServerClient;

public class LoginCommand implements ConsoleCommand {
    private String username;
    private String password;
    private final ServerClient client = new ServerClient();

    @Override
    public void setArgs(String[] args) {
        for (int i = 1; i < args.length; i++) {
            if ("--username".equals(args[i]) && i + 1 < args.length) {
                username = args[i + 1];
            }
            if ("--password".equals(args[i]) && i + 1 < args.length) {
                password = args[i + 1];
            }
        }
    }

    @Override
    public void execute() {
        if (username == null || password == null) {
            System.out.println("Ошибка: Требуются параметры --username и --password.");
            return;
        }

        String response = client.login(username, password);
        System.out.println(response);
    }
}
