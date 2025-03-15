package org.example.cli.models;

public class ComputeResource {
    private int cpuCores;
    private int diskSpace;
    private int ram;

    public ComputeResource(int cpuCores, int diskSpace, int ram) {
        this.cpuCores = cpuCores;
        this.diskSpace = diskSpace;
        this.ram = ram;
    }

    public int getCpuCores() {
        return cpuCores;
    }

    public int getDiskSpace() {
        return diskSpace;
    }

    public int getRam() {
        return ram;
    }
}

