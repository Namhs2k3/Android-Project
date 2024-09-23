package com.example.project_management;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DevTaskAdapter extends RecyclerView.Adapter<DevTaskAdapter.DevTaskViewHolder> {

    private List<DevTask> devTaskList;

    public DevTaskAdapter(List<DevTask> devTaskList) {
        this.devTaskList = devTaskList;
    }

    @NonNull
    @Override
    public DevTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dev_task, parent, false);
        return new DevTaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DevTaskViewHolder holder, int position) {
        DevTask devTask = devTaskList.get(position);
        holder.tvDevName.setText(devTask.getDevName());
        holder.tvTaskId.setText("Task ID: " + devTask.getTaskId());
        holder.tvStartDate.setText("Start Date: " + devTask.getStartDate());
        holder.tvEndDate.setText("End Date: " + devTask.getEndDate());
        holder.tvTaskName.setText(devTask.getTaskName());
        holder.tvEstimateDay.setText(String.valueOf(devTask.getEstimateDay()));
    }

    @Override
    public int getItemCount() {
        return devTaskList.size();
    }

    public static class DevTaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvDevName, tvTaskId, tvStartDate, tvEndDate, tvTaskName, tvEstimateDay;

        public DevTaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDevName = itemView.findViewById(R.id.tvDevName);
            tvTaskId = itemView.findViewById(R.id.tvTaskId);
            tvStartDate = itemView.findViewById(R.id.tvStartDate);
            tvEndDate = itemView.findViewById(R.id.tvEndDate);
            tvTaskName = itemView.findViewById(R.id.tvTaskName);
            tvEstimateDay = itemView.findViewById(R.id.tvEstimateDay);
        }
    }
}
