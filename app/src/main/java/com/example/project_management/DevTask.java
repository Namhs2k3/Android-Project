package com.example.project_management;

public class DevTask {
    private int id;
    private String devName;
    private int taskId;
    private String startDate;
    private String endDate;
    private String taskName;
    private int estimateDay;
    public DevTask(int id, String devName, int taskId, String startDate, String endDate, String taskName, int estimateDay) {
        this.id = id;
        this.devName = devName;
        this.taskId = taskId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.taskName = taskName;
        this.estimateDay = estimateDay;
    }

    public int getId() {
        return id;
    }

    public String getDevName() {
        return devName;
    }

    public int getTaskId() {
        return taskId;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getTaskName() {
        return taskName;
    }

    public int getEstimateDay() {
        return estimateDay;
    }
}
