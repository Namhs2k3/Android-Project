package com.example.project_management;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_management.Database.DatabaseHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DevTaskAdapter2 extends RecyclerView.Adapter<DevTaskAdapter2.DevTaskViewHolder> {

    private List<DevTask> devTaskList;
    private List<DevTask> devTaskListFull; // Danh sách đầy đủ để lưu trữ tất cả các task
    private Context context;
    private DatabaseHelper dbHelper;
    private List<Integer> selectedTaskIds = new ArrayList<>();

    public DevTaskAdapter2(List<DevTask> devTaskList, List<DevTask> devTaskListFull, Context context, DatabaseHelper dbHelper) {
        this.devTaskList = devTaskList;
        this.devTaskListFull = new ArrayList<>(devTaskList); // Sao lưu danh sách gốc
        this.context = context;
        this.dbHelper = dbHelper;
    }

    public DevTaskAdapter2(DeleteDevTaskActivity deleteDevTaskActivity, List<DevTask> devTaskList, List<DevTask> selectedDevTasks) {
    }

    @NonNull
    @Override
    public DevTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_list_item, parent, false);
        return new DevTaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DevTaskViewHolder holder, int position) {
        DevTask devTask = devTaskList.get(position);

        holder.taskIdText.setText( devTask.getTaskName());
        holder.taskNameText.setText("Assignee: " + devTask.getDevName());

        // Thiết lập checkbox
        holder.taskCheckbox.setChecked(selectedTaskIds.contains(devTask.getId()));

        holder.taskCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Thêm taskID vào danh sách nếu checkbox được chọn
                if (!selectedTaskIds.contains(devTask.getTaskId())) {
                    selectedTaskIds.add(devTask.getTaskId());
                }
            } else {
                // Loại bỏ taskID khỏi danh sách nếu checkbox không được chọn
                selectedTaskIds.remove(Integer.valueOf(devTask.getTaskId()));
            }
        });

        // Event click to edit or delete
        holder.itemView.setOnClickListener(v -> {
            showEditDialog(devTask, position);
        });
    }

    public void deleteSelectedTasks() {
        for (int taskId : selectedTaskIds) {
            dbHelper.deleteDevTask(taskId); // Gọi phương thức xóa từ cơ sở dữ liệu
            // Cũng cần phải xóa khỏi danh sách hiển thị
            // Tìm kiếm DevTask trong danh sách và xóa
            for (int i = 0; i < devTaskList.size(); i++) {
                if (devTaskList.get(i).getTaskId() == taskId) {
                    devTaskList.remove(i);
                    notifyItemRemoved(i);
                    break; // Dừng vòng lặp sau khi đã xóa
                }
            }
        }
        // Xóa tất cả các taskID đã chọn
        selectedTaskIds.clear();
    }
    private void showEditDialog(DevTask task, int position) {
        // Create dialog to edit task
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_task, null);
        builder.setView(view);

        EditText etDevName = view.findViewById(R.id.etDevName);
        Spinner spinnerTaskName = view.findViewById(R.id.spinnerTaskName);
        EditText etStartDate = view.findViewById(R.id.etStartDate);
        EditText etEndDate = view.findViewById(R.id.etEndDate);
        EditText etEstimateDay = view.findViewById(R.id.etEstimateDay);

        etStartDate.setOnClickListener(v -> showDateTimePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDateTimePicker(etEndDate));

        // Lấy danh sách tasks từ cơ sở dữ liệu
        List<Task> taskList = dbHelper.getAllTasks();
        List<String> taskNames = new ArrayList<>();
        for (Task t : taskList) {
            taskNames.add(t.getTaskName());
        }

        // Thiết lập adapter cho Spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, taskNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTaskName.setAdapter(spinnerAdapter);

        // Thiết lập thông tin ban đầu
        etDevName.setText(task.getDevName());
        etStartDate.setText(task.getStartDate());
        etEndDate.setText(task.getEndDate());
        etEstimateDay.setText(String.valueOf(task.getEstimateDay()));

        // Lấy Task ID và Estimate Day từ Task Name đã chọn
        int[] selectedTaskId = {0};
        spinnerTaskName.setSelection(taskNames.indexOf(task.getTaskName()));
        spinnerTaskName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Task selectedTask = taskList.get(position);
                selectedTaskId[0] = selectedTask.getTaskID();
                etEstimateDay.setText(String.valueOf(selectedTask.getEstimateDay()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                selectedTaskId[0] = 0; // Default value
            }
        });

        builder.setTitle("Edit Task")
                .setPositiveButton("Save", (dialog, which) -> {
                    String devName = etDevName.getText().toString(); // Lấy tên developer
                    updateTask(position, task.getId(), devName, selectedTaskId[0], etStartDate.getText().toString(), etEndDate.getText().toString());
                })
                .setNegativeButton("Delete", (dialog, which) -> {
                    deleteTask(task.getId(), position);
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
            return devTaskList != null ? devTaskList.size() : 0; // Trả về 0 nếu devTaskList là null
    }

    // Method to update task
    // Method to update task
    private void updateTask(int position, long id, String devName, int taskId, String startDate, String endDate) {
        // Cập nhật vào cơ sở dữ liệu
        dbHelper.updateDevTask(id, devName, taskId, startDate, endDate);

        // Cập nhật danh sách devTaskList
        DevTask updatedTask = new DevTask(id, devName, taskId, startDate, endDate, getTaskNameById(taskId), getEstimateDayById(taskId));

        // Thay đổi task tại vị trí đã cho
        devTaskList.set(position, updatedTask);

        // Cập nhật adapter
        notifyItemChanged(position); // Không cần gọi adapter
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
        dbHelper.deleteDevTask((int) id); // Xóa task từ cơ sở dữ liệu

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
        CheckBox taskCheckbox;
        TextView taskIdText;
        TextView taskNameText;

        public DevTaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskCheckbox = itemView.findViewById(R.id.taskCheckbox);
            taskIdText = itemView.findViewById(R.id.taskIdText);
            taskNameText = itemView.findViewById(R.id.taskNameText);
        }
    }

}

