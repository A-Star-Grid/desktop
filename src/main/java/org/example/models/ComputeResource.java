package org.example.models;

public class ComputeResource {
    private Integer cpuCores;
    private Integer diskSpace;
    private Integer ram;

    public ComputeResource() {
        this.cpuCores = 0;
        this.diskSpace = 0;
        this.ram = 0;
    }

    public ComputeResource(Integer cpuCores, Integer diskSpace, Integer ram) {
        this.cpuCores = cpuCores;
        this.diskSpace = diskSpace;
        this.ram = ram;
    }

    public ComputeResource(ComputeResource computeResource) {
        this.cpuCores = computeResource.cpuCores;
        this.diskSpace = computeResource.diskSpace;
        this.ram = computeResource.ram;
    }

    public ComputeResource add(ComputeResource other) {
        this.cpuCores += other.cpuCores;
        this.diskSpace += other.diskSpace;
        this.ram += other.ram;
        return this;
    }

    public ComputeResource subtract(ComputeResource other) {
        this.cpuCores = this.cpuCores - other.cpuCores;
        this.diskSpace = this.diskSpace - other.diskSpace;
        this.ram = this.ram - other.ram;
        return this;
    }

    public ComputeResource negative() {
        this.cpuCores = - this.cpuCores;
        this.diskSpace = - this.diskSpace;
        this.ram = - this.ram;
        return this;
    }

    public static ComputeResource max(ComputeResource a, ComputeResource b) {
        return new ComputeResource(
                Math.max(a.cpuCores, b.cpuCores),
                Math.max(a.diskSpace, b.diskSpace),
                Math.max(a.ram, b.ram)
        );
    }

    public Integer getCpuCores() {
        return cpuCores;
    }

    public void setCpuCores(Integer cpuCores) {
        this.cpuCores = cpuCores;
    }

    public Integer getRam() {
        return ram;
    }

    public void setRam(Integer ram) {
        this.ram = ram;
    }

    public Integer getDiskSpace() {
        return diskSpace;
    }

    public void setDiskSpace(Integer diskSpace) {
        this.diskSpace = diskSpace;
    }
}
