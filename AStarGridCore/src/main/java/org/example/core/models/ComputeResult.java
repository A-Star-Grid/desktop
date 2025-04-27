package org.example.core.models;

public class ComputeResult {
    private String resultPath;
    private Boolean isSuccess;

    public ComputeResult() {
    }

    public ComputeResult(String resultPath, Boolean isSuccess) {
        this.resultPath = resultPath;
        this.isSuccess = isSuccess;
    }

    public String getResultPath() {
        return resultPath;
    }

    public void setResultPath(String resultPath) {
        this.resultPath = resultPath;
    }

    public Boolean getSuccess() {
        return isSuccess;
    }

    public void setSuccess(Boolean success) {
        isSuccess = success;
    }
}
