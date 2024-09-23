package com.example.project_management;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_management.Database.DatabaseHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DevTaskAdapter adapter;
    private List<DevTask> devTaskList;
    private DatabaseHelper dbHelper;
    private Button btnAddDevTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewDevTask);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DatabaseHelper(this);

        // Thêm dữ liệu mẫu nếu cần thiết
        dbHelper.insertSampleData(dbHelper.getWritableDatabase());

        // Lấy danh sách dev_task từ cơ sở dữ liệu
        loadDevTasks();

        adapter = new DevTaskAdapter(devTaskList, this, dbHelper);
        recyclerView.setAdapter(adapter);

        btnAddDevTask = findViewById(R.id.btnAddDevTask);
        btnAddDevTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDevTaskDialog();
            }
        });
    }

    private void showAddDevTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_dev_task, null);

        EditText etDevName = view.findViewById(R.id.addDevName);
        EditText etTaskId = view.findViewById(R.id.addTaskId);
        EditText etStartDate = view.findViewById(R.id.addStartDate);
        EditText etEndDate = view.findViewById(R.id.addEndDate);
        EditText etTaskName = view.findViewById(R.id.addTaskName);
        EditText etEstimateDay = view.findViewById(R.id.addEstimateDay);

        // Thiết lập DatePicker cho Start Date
        etStartDate.setOnClickListener(v -> showDateTimePicker(etStartDate));

        // Thiết lập DatePicker cho End Date
        etEndDate.setOnClickListener(v -> showDateTimePicker(etEndDate));

        builder.setView(view)
                .setTitle("Add Dev Task")
                .setPositiveButton("Add", (dialog, which) -> {
                    String devName = etDevName.getText().toString();
                    int taskId = Integer.parseInt(etTaskId.getText().toString());
                    String startDate = etStartDate.getText().toString();
                    String endDate = etEndDate.getText().toString();
                    String taskName = etTaskName.getText().toString();
                    int estimateDay = Integer.parseInt(etEstimateDay.getText().toString());

                    // Thêm task mới vào cơ sở dữ liệu
                    long result = dbHelper.insertDevTask(devName, taskId, startDate, endDate);

                    if (result != -1) { // Kiểm tra nếu thêm thành công
                        // Cập nhật danh sách
                        devTaskList.add(new DevTask(result, devName, taskId, startDate, endDate, taskName, estimateDay));
                        adapter.notifyItemInserted(devTaskList.size() - 1); // Thông báo adapter về sự thay đổi
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showDateTimePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timeView, selectedHour, selectedMinute) -> {
                String dateTime = String.format("%02d/%02d/%04d %02d:%02d", selectedDay, selectedMonth + 1, selectedYear, selectedHour, selectedMinute);
                editText.setText(dateTime); // Thay vì textView.setText(dateTime);
            }, hour, minute, true);
            timePickerDialog.show();
        }, year, month, day);

        datePickerDialog.show();
    }


    private void loadDevTasks() {
        devTaskList = new ArrayList<>();
        Cursor cursor = dbHelper.getDevTasksWithDetails();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("ID"));
                String devName = cursor.getString(cursor.getColumnIndexOrThrow("DEV_NAME"));
                int taskId = cursor.getInt(cursor.getColumnIndexOrThrow("TASKID"));
                String startDate = cursor.getString(cursor.getColumnIndexOrThrow("STARTDATE"));
                String endDate = cursor.getString(cursor.getColumnIndexOrThrow("ENDDATE"));
                String taskName = cursor.getString(cursor.getColumnIndexOrThrow("TASK_NAME"));
                int estimateDay = cursor.getInt(cursor.getColumnIndexOrThrow("ESTIMATE_DAY"));

                // Thêm vào danh sách
                devTaskList.add(new DevTask(id, devName, taskId, startDate, endDate, taskName, estimateDay));
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        // Sắp xếp danh sách theo Task ID
        devTaskList.sort((task1, task2) -> Integer.compare(task1.getTaskId(), task2.getTaskId()));
    }
}
