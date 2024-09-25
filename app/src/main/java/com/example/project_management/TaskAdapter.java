package com.example.project_management;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private Context context;
    private List<Task> taskList;
    private List<Task> selectedTasks;

    public TaskAdapter(Context context, List<Task> taskList, List<Task> selectedTasks) {
        this.context = context;
        this.taskList = taskList;
        this.selectedTasks = selectedTasks;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.task_list_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.taskNameText.setText(task.getTaskName());

        // Set checkbox state
        holder.taskCheckbox.setChecked(selectedTasks.contains(task));

        // Handle checkbox selection
        holder.taskCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedTasks.add(task);
            } else {
                selectedTasks.remove(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView taskNameText;
        CheckBox taskCheckbox;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskNameText = itemView.findViewById(R.id.taskNameText);
            taskCheckbox = itemView.findViewById(R.id.taskCheckbox);
        }
    }
}
