package org.example.models.dto;

import org.example.models.ComputeResource;
import org.example.models.shedule.ScheduleInterval;

import java.util.List;
import java.util.UUID;

public class SubscribeTransport {
    private List<ScheduleInterval> scheduleIntervals;
    private UUID deviceUuid;

    public SubscribeTransport() {
    }

    public SubscribeTransport(SubscribeRequest subscribeRequest, UUID deviceUuid){
        scheduleIntervals = subscribeRequest.getScheduleIntervals();
        this.deviceUuid = deviceUuid;
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
