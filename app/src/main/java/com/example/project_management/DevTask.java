package com.example.project_management;

import android.os.Parcel;
import android.os.Parcelable;

public class DevTask implements Parcelable {
    private long id;
    private String devName;
    private int taskId;
    private String startDate;
    private String endDate;
    private String taskName;
    private int estimateDay;

    public DevTask(long id, String devName, int taskId, String startDate, String endDate, String taskName, int estimateDay) {
        this.id = id;
        this.devName = devName;
        this.taskId = taskId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.taskName = taskName;
        this.estimateDay = estimateDay;
    }

    protected DevTask(Parcel in) {
        id = in.readLong();
        devName = in.readString();
        taskId = in.readInt();
        startDate = in.readString();
        endDate = in.readString();
        taskName = in.readString();
        estimateDay = in.readInt();
    }

    public static final Creator<DevTask> CREATOR = new Creator<DevTask>() {
        @Override
        public DevTask createFromParcel(Parcel in) {
            return new DevTask(in);
        }

        @Override
        public DevTask[] newArray(int size) {
            return new DevTask[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(devName);
        dest.writeInt(taskId);
        dest.writeString(startDate);
        dest.writeString(endDate);
        dest.writeString(taskName);
        dest.writeInt(estimateDay);
    }

    // Getters and Setters
    public long getId() {
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

    // Setters
    public void setId(long id) {
        this.id = id;
    }

    public void setDevName(String devName) {
        this.devName = devName;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setEstimateDay(int estimateDay) {
        this.estimateDay = estimateDay;
    }
}
