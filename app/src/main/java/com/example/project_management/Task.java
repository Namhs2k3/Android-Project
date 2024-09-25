package com.example.project_management;

public class Task {
    private int taskID;
    private String taskName;
    private int estimateDay;

    // Constructor
    public Task(int taskID, String taskName, int estimateDay) {
        this.taskID = taskID;
        this.taskName = taskName;
        this.estimateDay = estimateDay;
    }

    // Getters
    public int getTaskID() {
        return taskID;
    }

    public String getTaskName() {
        return taskName;
    }

    public int getEstimateDay() {
        return estimateDay;
    }

    // Setters (nếu cần)
    public void setTaskID(int taskID) {
        this.taskID = taskID;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setEstimateDay(int estimateDay) {
        this.estimateDay = estimateDay;
    }
}

