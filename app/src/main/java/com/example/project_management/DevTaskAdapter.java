package com.example.project_management;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_management.Database.DatabaseHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DevTaskAdapter extends RecyclerView.Adapter<DevTaskAdapter.DevTaskViewHolder> {

    private List<DevTask> devTaskList;
    private List<DevTask> devTaskListFull; // Danh sách đầy đủ để lưu trữ tất cả các task
    private Context context;
    private DatabaseHelper dbHelper;

    public DevTaskAdapter(List<DevTask> devTaskList, List<DevTask> devTaskListFull, Context context, DatabaseHelper dbHelper) {
        this.devTaskList = devTaskList;
        this.devTaskListFull = new ArrayList<>(devTaskList); // Sao lưu danh sách gốc
        this.context = context;
        this.dbHelper = dbHelper;
    }

    public DevTaskAdapter(DeleteDevTaskActivity deleteDevTaskActivity, List<DevTask> devTaskList, List<DevTask> selectedDevTasks) {
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
        holder.tvTaskName.setText(devTask.getTaskName());

        if (showEstimateDay) {
            holder.tvEstimateDay.setVisibility(View.VISIBLE);
            holder.tvEstimateDay.setText("Estimate Day: " + devTask.getEstimateDay());
        } else {
            holder.tvEstimateDay.setVisibility(View.GONE);
        }

        // Event click to edit or delete
        holder.itemView.setOnClickListener(v -> {
            showEditDialog(devTask, position);
        });
    }

    private void showEditDialog(DevTask task, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_task, null);
        builder.setView(view);

        EditText etDevName = view.findViewById(R.id.etDevName);
        EditText etTaskName = view.findViewById(R.id.etTaskName);
        EditText etStartDate = view.findViewById(R.id.etStartDate);
        EditText etEndDate = view.findViewById(R.id.etEndDate);
        EditText etEstimateDay = view.findViewById(R.id.etEstimateDay);

        // Thiết lập DatePicker cho Start Date
        etStartDate.setOnClickListener(v -> showDateTimePicker(etStartDate, () -> updateEstimateDay(etStartDate, etEndDate, etEstimateDay)));

        // Thiết lập DatePicker cho End Date
        etEndDate.setOnClickListener(v -> showDateTimePicker(etEndDate, () -> updateEstimateDay(etStartDate, etEndDate, etEstimateDay)));

        // Thiết lập thông tin ban đầu
        etDevName.setText(task.getDevName());
        etTaskName.setText(task.getTaskName()); // Hiển thị tên task
        etStartDate.setText(task.getStartDate());
        etEndDate.setText(task.getEndDate());
        etEstimateDay.setText(String.valueOf(task.getEstimateDay()));

        builder.setTitle("Edit Dev Task")
                .setPositiveButton("Save", (dialog, which) -> {
                    String devName = etDevName.getText().toString();
                    String taskName = etTaskName.getText().toString().trim(); // Nhập tên task từ bàn phím

                    if(!isTaskNameAndDevNameValid(taskName, devName)){
                        Toast.makeText(context, "Both Task name and Dev name can not be null", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Kiểm tra trùng tên task
                    if (dbHelper.isTaskNameExists(taskName) && !taskName.equals(task.getTaskName())) {
                        Toast.makeText(context, "Task name already exists!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(!isStartDateOrEndDateValid(etStartDate.getText().toString(), etEndDate.getText().toString())){
                        Toast.makeText(context, "Both Start Date and End Date must either be both empty or both have values. If one has a value, the other must also have a value.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (!isValidDateRange(etStartDate.getText().toString(), etEndDate.getText().toString())) {
                        Toast.makeText(context, "End Date must be greater than or equal to Start Date.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DevTask currentTask = devTaskList.get(position); // Lấy task hiện tại
                    updateTask(position, currentTask.getId(), devName, taskName, etStartDate.getText().toString(), etEndDate.getText().toString(), Integer.parseInt(etEstimateDay.getText().toString()), currentTask.getTaskId());
                    Toast.makeText(context, "Task Updated Successfully!", Toast.LENGTH_LONG).show();

                })
                .setNegativeButton("Delete", (dialog, which) -> {
                    // Hiển thị hộp thoại xác nhận
                    new AlertDialog.Builder(context)
                            .setTitle("Confirm Delete")
                            .setMessage("Are you sure you want to delete this task?")
                            .setPositiveButton("OK", (confirmDialog, confirmWhich) -> {
                                // Nếu người dùng chọn "OK", xóa task
                                deleteTask(task.getId(), position);
                                Toast.makeText(context, "Task Deleted Successfully.", Toast.LENGTH_LONG).show();
                            })
                            .setNegativeButton("Cancel", (confirmDialog, confirmWhich) -> {
                                // Nếu người dùng chọn "Cancel", đóng hộp thoại xác nhận
                                confirmDialog.dismiss();
                            })
                            .show();
                })
                .setNeutralButton("Cancel", null);

        builder.create().show();
    }

    public boolean isStartDateOrEndDateValid(String startDate, String endDate) {
        // Trả về true nếu cả startDate và endDate đều không trống, hoặc cả hai đều trống
        return (!startDate.isEmpty() && !endDate.isEmpty()) || (startDate.isEmpty() && endDate.isEmpty());
    }

    public boolean isTaskNameAndDevNameValid(String taskName, String devName){
        return !(taskName.isEmpty() || devName.isEmpty());
    }
    // Hàm để cập nhật estimateDay
    private void updateEstimateDay(EditText etStartDate, EditText etEndDate, EditText etEstimateDay) {
        String startDate = etStartDate.getText().toString();
        String endDate = etEndDate.getText().toString();

        // Tính Estimate Day
        if (!startDate.isEmpty() && !endDate.isEmpty()) {
            int estimateDay = calculateEstimateDays(startDate, endDate);
            etEstimateDay.setText(String.valueOf(estimateDay)); // Cập nhật EditText với giá trị tính toán
        } else {
            etEstimateDay.setText(""); // Nếu một trong hai rỗng, xóa giá trị estimateDay
        }
    }

    public boolean isValidDateRange(String startDateStr, String endDateStr) {
        if (startDateStr.isEmpty() || endDateStr.isEmpty()) {
            return true; // Không cần kiểm tra nếu một trong hai trống
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date startDate = dateFormat.parse(startDateStr);
            Date endDate = dateFormat.parse(endDateStr);
            return startDate != null && endDate != null && !startDate.after(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }


    private void showDateTimePicker(EditText editText, Runnable onDateSet) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, selectedYear, selectedMonth, selectedDay) -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(context, (timeView, selectedHour, selectedMinute) -> {
                String dateTime = String.format("%02d/%02d/%04d %02d:%02d", selectedDay, selectedMonth + 1, selectedYear, selectedHour, selectedMinute);
                editText.setText(dateTime); // Cập nhật EditText với ngày giờ đã chọn

                // Gọi callback để cập nhật estimateDay
                onDateSet.run();
            }, hour, minute, true);
            timePickerDialog.show();
        }, year, month, day);

        datePickerDialog.show();
    }



    @Override
    public int getItemCount() {
        return devTaskList != null ? devTaskList.size() : 0; // Trả về 0 nếu devTaskList là null
    }

    // Method to update task
    public void updateTask(int position, long id, String devName, String taskName, String startDate, String endDate, int estimateDay , int taskId) {
        dbHelper.updateDevTask(id, devName, taskName, startDate, endDate,estimateDay, taskId);
        DevTask updatedTask = new DevTask(id, devName, taskName, startDate, endDate, estimateDay,taskId);
        // Cập nhật danh sách devTaskList
        devTaskList.set(position, updatedTask);
        // Cập nhật lại adapter
        notifyItemChanged(position);
    }

    public int calculateEstimateDays(String startDateStr, String endDateStr) {
        if (startDateStr.isEmpty() || endDateStr.isEmpty()) {
            return 0; // Không tính estimate nếu một trong hai trống
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date startDate = dateFormat.parse(startDateStr);
            Date endDate = dateFormat.parse(endDateStr);

            if (startDate != null && endDate != null) {
                long differenceInMillis = endDate.getTime() - startDate.getTime();
                return (int) (differenceInMillis / (1000 * 60 * 60 * 24))+1; // Số ngày
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private String getTaskNameById(int taskId) {
        // Hàm lấy tên task dựa vào taskId
        Task task = dbHelper.getTaskById(taskId); // Cần định nghĩa phương thức này trong DatabaseHelper
        return task != null ? task.getTaskName() : "";
    }

    private int getEstimateDayById(int taskId) {
        // Hàm lấy estimate day dựa vào taskId
        Task task = dbHelper.getTaskById(taskId); // Cần định nghĩa phương thức này trong DatabaseHelper
        return task != null ? task.getEstimateDay() : 0;
    }



    private void deleteTask(long id, int position) {
        DevTask currentTask = devTaskList.get(position);
        dbHelper.deleteDevTask((int) id, currentTask.getTaskId()); // Xóa task từ cơ sở dữ liệu

        // Xóa task khỏi danh sách đầy đủ
        DevTask taskToRemove = devTaskList.get(position);
        devTaskList.remove(position);
        devTaskListFull.remove(taskToRemove); // Đồng bộ với danh sách đầy đủ

        notifyItemRemoved(position); // Thông báo adapter về sự thay đổi
        notifyItemRangeChanged(position, devTaskList.size()); // Cập nhật lại danh sách
    }


    public void filter(String text) {
        List<DevTask> filteredList = new ArrayList<>();

        if (text.isEmpty()) {
            filteredList.addAll(devTaskListFull); // Hiển thị lại toàn bộ danh sách khi không có từ khóa
        } else {
            text = text.toLowerCase();
            for (DevTask task : devTaskListFull) {
                if (task.getTaskName().toLowerCase().contains(text) || task.getDevName().toLowerCase().contains(text)) {
                    filteredList.add(task);
                }
            }
        }

        devTaskList.clear();
        devTaskList.addAll(filteredList);
        notifyDataSetChanged(); // Thông báo adapter rằng dữ liệu đã thay đổi
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

    private boolean showEstimateDay = true; // Default to showing estimate day

    public void setShowEstimateDay(boolean show) {
        this.showEstimateDay = show;
        notifyDataSetChanged(); // Refresh the RecyclerView
    }
}

