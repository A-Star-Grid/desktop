package org.example.core.models.dto;

import org.example.core.models.shedule.ScheduleInterval;

import java.util.List;
import java.util.UUID;

public class SubscribeTransport {
    private List<ScheduleInterval> scheduleIntervals;
    private UUID deviceUuid;
    private Integer projectId;

    public SubscribeTransport() {
    }

    public SubscribeTransport(SubscribeRequest subscribeRequest, UUID deviceUuid){
        scheduleIntervals = subscribeRequest.getScheduleIntervals();
        this.deviceUuid = deviceUuid;
        this.projectId = subscribeRequest.getProjectId();
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public List<ScheduleInterval> getScheduleIntervals() {
        return scheduleIntervals;
    }

    public void setScheduleIntervals(List<ScheduleInterval> cronSchedule) {
        this.scheduleIntervals = cronSchedule;
    }

    public UUID getDeviceUuid() {
        return deviceUuid;
    }

    public void setDeviceUuid(UUID deviceUuid) {
        this.deviceUuid = deviceUuid;
    }
}
