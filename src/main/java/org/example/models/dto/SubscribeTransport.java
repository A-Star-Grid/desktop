package org.example.models.dto;

import java.util.UUID;

public class SubscribeTransport {
    private Integer cpuCores;
    private String  cronSchedule;
    private Integer diskSpace;
    private Integer projectId;
    private Integer ram;
    private UUID deviceUUID;

    public SubscribeTransport() {
    }

    public SubscribeTransport(SubscribeRequest subscribeRequest, UUID deviceUUID){
        cpuCores = subscribeRequest.getCpuCores();
        cronSchedule = subscribeRequest.getCronSchedule();
        diskSpace = subscribeRequest.getDiskSpace();
        projectId = subscribeRequest.getProjectId();
        ram = subscribeRequest.getRam();
        this.deviceUUID = deviceUUID;
    }

    public Integer getCpuCores() {
        return cpuCores;
    }

    public void setCpuCores(Integer cpuCores) {
        this.cpuCores = cpuCores;
    }

    public String getCronSchedule() {
        return cronSchedule;
    }

    public void setCronSchedule(String cronSchedule) {
        this.cronSchedule = cronSchedule;
    }

    public Integer getDiskSpace() {
        return diskSpace;
    }

    public void setDiskSpace(Integer diskSpace) {
        this.diskSpace = diskSpace;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getRam() {
        return ram;
    }

    public void setRam(Integer ram) {
        this.ram = ram;
    }

    public UUID getDeviceUUID() {
        return deviceUUID;
    }

    public void setDeviceUUID(UUID deviceUUID) {
        this.deviceUUID = deviceUUID;
    }
}
