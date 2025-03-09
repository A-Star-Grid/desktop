package org.example;

public class SetLimitsCommand implements ConsoleCommand {
    private int ram;
    private double cpu;
    private int disk;
    private final ServerClient client = new ServerClient();

    @Override
    public void setArgs(String[] args) {
        for (int i = 1; i < args.length; i++) {
            if ("--ram".equals(args[i]) && i + 1 < args.length) {
                ram = Integer.parseInt(args[i + 1]);
            }
            if ("--cpu".equals(args[i]) && i + 1 < args.length) {
                cpu = Double.parseDouble(args[i + 1]);
            }
            if ("--disk".equals(args[i]) && i + 1 < args.length) {
                disk = Integer.parseInt(args[i + 1]);
            }
        }
    }

    @Override
    public void execute() {
        if (ram > 0) client.setRamLimit(ram);
        if (cpu > 0) client.setCpuLimit(cpu);
        if (disk > 0) client.setDiskLimit(disk);
        System.out.println("Лимиты установлены.");
    }
}
