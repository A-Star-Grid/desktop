package org.example.models.dto;

import java.util.UUID;

public class UploadResult {
    private int statusCode;
    private String message;
    private int projectId;
    private UUID deviceUuid;

    public UploadResult() {
    }

    public UploadResult(int statusCode, String message, int projectId, UUID deviceUuid) {
        this.statusCode = statusCode;
        this.message = message;
        this.projectId = projectId;
        this.deviceUuid = deviceUuid;
    }

    public int getStatusCode() { return statusCode; }
    public String getMessage() { return message; }
    public int getProjectId() { return projectId; }
    public UUID getDeviceUuid() { return deviceUuid; }

    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }

    @Override
    public String toString() {
        return "Status Code: " + statusCode +
                "\nMessage: " + message +
                "\nProject ID: " + projectId +
                "\nDevice UUID: " + deviceUuid;
    }
}

