package org.example.models.dto;

public class CurrentSettingsResponse {

    private int ramLimit;
    private double cpuLimit;
    private int diskLimit;
    private String virtualBoxPath;
    private boolean computationActive;

    public CurrentSettingsResponse() {
    }

    public CurrentSettingsResponse(int ramLimit,
                                   double cpuLimit,
                                   int diskLimit,
                                   String virtualBoxPath,
                                   boolean computationActive) {
        this.ramLimit = ramLimit;
        this.cpuLimit = cpuLimit;
        this.diskLimit = diskLimit;
        this.virtualBoxPath = virtualBoxPath;
        this.computationActive = computationActive;
    }

    public int getRamLimit() {
        return ramLimit;
    }

    public void setRamLimit(int ramLimit) {
        this.ramLimit = ramLimit;
    }

    public double getCpuLimit() {
        return cpuLimit;
    }

    public void setCpuLimit(double cpuLimit) {
        this.cpuLimit = cpuLimit;
    }

    public int getDiskLimit() {
        return diskLimit;
    }

    public void setDiskLimit(int diskLimit) {
        this.diskLimit = diskLimit;
    }

    public String getVirtualBoxPath() {
        return virtualBoxPath;
    }

    public void setVirtualBoxPath(String virtualBoxPath) {
        this.virtualBoxPath = virtualBoxPath;
    }

    public boolean isComputationActive() {
        return computationActive;
    }

    public void setComputationActive(boolean computationActive) {
        this.computationActive = computationActive;
    }
}
