package org.example.core.models.dto;

import org.example.core.models.shedule.ScheduleInterval;

import java.util.List;

public class SubscribeRequest {
    private List<ScheduleInterval> scheduleIntervals;
    private Integer projectId;

    public List<ScheduleInterval> getScheduleIntervals() {
        return scheduleIntervals;
    }

    public void setScheduleIntervals(List<ScheduleInterval> cronSchedule) {
        this.scheduleIntervals = cronSchedule;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

}
