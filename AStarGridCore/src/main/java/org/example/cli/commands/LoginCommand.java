package org.example.cli.commands;


import org.example.cli.ServerClient;

public class LoginCommand implements ConsoleCommand {
    private String username;
    private String password;
    private final ServerClient client;

    public LoginCommand(){
        client =  new ServerClient();
    }

    public LoginCommand(ServerClient client){
        this.client =  client;
    }

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ServerClient getClient() {
        return client;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
