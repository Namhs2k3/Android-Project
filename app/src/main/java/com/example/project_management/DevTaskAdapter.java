package com.example.project_management;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_management.Database.DatabaseHelper;

import java.util.Calendar;
import java.util.List;

public class DevTaskAdapter extends RecyclerView.Adapter<DevTaskAdapter.DevTaskViewHolder> {

    private List<DevTask> devTaskList;
    private Context context;
    private DatabaseHelper dbHelper;

    public DevTaskAdapter(List<DevTask> devTaskList, Context context, DatabaseHelper dbHelper) {
        this.devTaskList = devTaskList;
        this.context = context;
        this.dbHelper = dbHelper;
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
        holder.tvDevName.setText("Assignee: " + devTask.getDevName());
        holder.tvTaskId.setText("Task ID: " + devTask.getTaskId());
        holder.tvStartDate.setText("Start Date: " + devTask.getStartDate());
        holder.tvEndDate.setText("End Date: " + devTask.getEndDate());
        holder.tvTaskName.setText("Task Name: " + devTask.getTaskName());
        holder.tvEstimateDay.setText("Estimate Day: " + devTask.getEstimateDay());

        // Event click to edit or delete
        holder.itemView.setOnClickListener(v -> {
            showEditDialog(devTask, position);
        });
    }

    private void showEditDialog(DevTask task, int position) {
        // Create dialog to edit task
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_task, null);
        builder.setView(view);

        EditText etTaskName = view.findViewById(R.id.etTaskName);
        EditText etStartDate = view.findViewById(R.id.etStartDate);
        EditText etEndDate = view.findViewById(R.id.etEndDate);
        EditText etDevName = view.findViewById(R.id.etDevName);
        EditText etEstimateDay = view.findViewById(R.id.etEstimateDay);

        etTaskName.setText(task.getTaskName());
        etStartDate.setText(task.getStartDate());
        etEndDate.setText(task.getEndDate());
        etDevName.setText(task.getDevName());
        etEstimateDay.setText(String.valueOf(task.getEstimateDay()));

        etStartDate.setOnClickListener(v -> showDateTimePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDateTimePicker(etEndDate));

        builder.setTitle("Edit Task")
                .setPositiveButton("Save", (dialog, which) -> {
                    long taskId = task.getTaskId(); // Lấy taskId từ task
                    int estimateDay = Integer.parseInt(etEstimateDay.getText().toString()); // Lấy estimateDay từ EditText
                    updateTask(position, task.getId(), etDevName.getText().toString(),
                            taskId, etStartDate.getText().toString(), etEndDate.getText().toString());
                })
                .setNegativeButton("Delete", (dialog, which) -> {
                    deleteTask(task.getId(), position); // Gọi deleteTask với ID của task
                })
                .setNeutralButton("Cancel", null);

        builder.create().show();
    }

    private void showDateTimePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, selectedYear, selectedMonth, selectedDay) -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(context, (timeView, selectedHour, selectedMinute) -> {
                String dateTime = String.format("%02d/%02d/%04d %02d:%02d", selectedDay, selectedMonth + 1, selectedYear, selectedHour, selectedMinute);
                editText.setText(dateTime); // Thay vì textView.setText(dateTime);
            }, hour, minute, true);
            timePickerDialog.show();
        }, year, month, day);

        datePickerDialog.show();
    }


    @Override
    public int getItemCount() {
        return devTaskList.size();
    }

    // Method to update task
    private void updateTask(int position, long id, String devName, long taskId, String startDate, String endDate) {
        dbHelper.updateDevTask(id, devName, taskId, startDate, endDate);

        // Update the list
        DevTask updatedTask = devTaskList.get(position);
        updatedTask.setDevName(devName);
        updatedTask.setTaskId((int) taskId); // Nếu taskId là long, chuyển đổi thành int nếu cần
        updatedTask.setStartDate(startDate);
        updatedTask.setEndDate(endDate);

        // Notify adapter about the change
        notifyItemChanged(position);
    }


    // Method to delete task
    private void deleteTask(long id, int position) {
        dbHelper.deleteDevTask((int) id); // Xóa task từ cơ sở dữ liệu
        devTaskList.remove(position); // Xóa task khỏi danh sách
        notifyItemRemoved(position); // Thông báo adapter về sự thay đổi
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

