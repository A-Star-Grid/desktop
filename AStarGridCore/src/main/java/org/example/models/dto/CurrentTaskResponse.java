package org.example.models.dto;

import java.util.UUID;

public class CurrentTaskResponse {
    private UUID taskUuid;

    public UUID getTaskUuid() {
        return taskUuid;
    }

    public void setTaskUuid(UUID taskUuid) {
        this.taskUuid = taskUuid;
    }
}
