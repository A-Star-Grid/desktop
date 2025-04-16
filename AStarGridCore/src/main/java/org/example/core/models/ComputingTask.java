package org.example.core.models;

import org.example.core.models.shedule.ScheduleInterval;

import java.util.Objects;
import java.util.UUID;

public class ComputingTask {
    private final Integer projectId;
    private final ScheduleInterval interval;
    private final UUID taskUuid;

    public ComputingTask(Integer projectId, ScheduleInterval interval, UUID taskUuid) {
        this.projectId = projectId;
        this.interval = interval;
        this.taskUuid = taskUuid;
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

    public UUID getTaskUuid() {
        return taskUuid;
    }
}
