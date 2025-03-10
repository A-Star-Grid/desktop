package org.example.models;

import java.util.List;

public class SubscribeRequest {
    private int projectId;
    private List<ScheduleInterval> scheduleIntervals;

    public SubscribeRequest(int projectId, List<ScheduleInterval> scheduleIntervals) {
        this.projectId = projectId;
        this.scheduleIntervals = scheduleIntervals;
    }

    public int getProjectId() {
        return projectId;
    }

    public List<ScheduleInterval> getScheduleIntervals() {
        return scheduleIntervals;
    }
}
