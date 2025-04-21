package org.example.cli.commands;

import org.example.cli.ServerClient;

public class SetCpuCommand implements ConsoleCommand {
    private double cpu;
    private final ServerClient client;

    public SetCpuCommand(){
        client = new ServerClient();
    }

    public SetCpuCommand(ServerClient client){
        this.client = client;
    }

    @Override
    public void setArgs(String[] args) {
        for (int i = 1; i < args.length; i++) {
            if ("--cpu".equals(args[i]) && i + 1 < args.length) {
                cpu = Double.parseDouble(args[i + 1]);
            }
        }
    }

    @Override
    public void execute() {
        if (cpu <= 0) {
            System.out.println("Error: need --cpu");
            return;
        }

        String response = client.setCpuLimit(cpu);
        System.out.println(response);
    }
}
