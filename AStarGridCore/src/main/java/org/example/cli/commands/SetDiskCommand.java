package org.example.cli.commands;


import org.example.cli.ServerClient;

public class SetDiskCommand implements ConsoleCommand {
    private int disk;
    private final ServerClient client;

    public SetDiskCommand(){
        client = new ServerClient();
    }

    public SetDiskCommand(ServerClient client){
        this.client = client;
    }

    @Override
    public void setArgs(String[] args) {
        for (int i = 1; i < args.length; i++) {
            if ("--disk".equals(args[i]) && i + 1 < args.length) {
                disk = Integer.parseInt(args[i + 1]);
            }
        }
    }

    @Override
    public void execute() {
        if (disk <= 0) {
            System.out.println("Error: need --disk");
            return;
        }

        String response = client.setDiskLimit(disk);
        System.out.println(response);
    }
}

