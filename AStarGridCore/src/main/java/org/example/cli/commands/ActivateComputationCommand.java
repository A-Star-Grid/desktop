package org.example.cli.commands;


import org.example.cli.ServerClient;

public class ActivateComputationCommand implements ConsoleCommand {
    private boolean activate;
    private final ServerClient client;

    public ActivateComputationCommand(){
        client = new ServerClient();
    }

    public ActivateComputationCommand(ServerClient client){
        this.client = client;
    }

    @Override
    public void setArgs(String[] args) {
        for (int i = 1; i < args.length; i++) {
            if ("--enable".equals(args[i])) {
                activate = true;
            } else if ("--disable".equals(args[i])) {
                activate = false;
            }
        }
    }

    @Override
    public void execute() {
        String response = client.setComputationState(activate);
        System.out.println(response);
    }
}
