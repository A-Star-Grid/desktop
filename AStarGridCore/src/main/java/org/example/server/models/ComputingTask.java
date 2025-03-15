package org.example.server.models;

import org.example.server.models.shedule.ScheduleInterval;

import java.util.Objects;

public class ComputingTask {
    private final Integer projectId;
    private final ScheduleInterval interval;

    public ComputingTask(Integer projectId, ScheduleInterval interval) {
        this.projectId = projectId;
        this.interval = interval;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass()) return false;
        var taskKey = (ComputingTask) o;
        return Objects.equals(projectId, taskKey.projectId) &&
                Objects.equals(interval, taskKey.interval);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, interval);
    }

    public Integer getProjectId() {
        return projectId;
    }

    public ScheduleInterval getInterval() {
        return interval;
    }
}
