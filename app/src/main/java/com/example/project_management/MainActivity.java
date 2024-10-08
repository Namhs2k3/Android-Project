package com.example.project_management;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;

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

        // Khởi tạo RecyclerView
        recyclerView = findViewById(R.id.recyclerViewDevTask);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Khởi tạo devTaskList
        devTaskList = new ArrayList<>();
        Log.d("MainActivity", "devTaskList initialized");

        // Thêm dữ liệu mẫu nếu cần thiết
        dbHelper.insertSampleData(dbHelper.getWritableDatabase());

        // Lấy danh sách dev_task từ cơ sở dữ liệu
        loadDevTasks();
        Log.d("MainActivity", "Tasks loaded: " + devTaskList.size());

        // Khởi tạo adapter
        adapter = new DevTaskAdapter(devTaskList, new ArrayList<>(devTaskList), this, dbHelper);
        recyclerView.setAdapter(adapter);
        Log.d("MainActivity", "Adapter initialized");

        // Thêm sự kiện cho nút Add
        btnAddDevTask = findViewById(R.id.btnAddDevTask);
        btnAddDevTask.setOnClickListener(v -> showAddDevTaskDialog());

        // Khởi tạo SearchView và thêm sự kiện
        SearchView searchView = findViewById(R.id.searchView);
        Log.d("MainActivity", searchView == null ? "SearchView is null" : "SearchView is OK");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("MainActivity", "Query submitted: " + query);
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("MainActivity", "Query text changed: " + newText);
                adapter.filter(newText);
                return true;
            }
        });

        // Khởi tạo button để chuyển đến GanttChartActivity
        Button btnViewGanttChart = findViewById(R.id.btnViewGanttChart);
        btnViewGanttChart.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GanttChartActivity.class);

            // Chuyển danh sách devTaskList qua Intent
            intent.putParcelableArrayListExtra("devTaskList", (ArrayList<? extends Parcelable>) devTaskList);

            startActivity(intent);
        });

    }



    private void showAddDevTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_dev_task, null);

        EditText etDevName = view.findViewById(R.id.addDevName);
        EditText etTaskId = view.findViewById(R.id.addTaskId);
        EditText etStartDate = view.findViewById(R.id.addStartDate);
        EditText etEndDate = view.findViewById(R.id.addEndDate);
        EditText etEstimateDay = view.findViewById(R.id.addEstimateDay);
        Spinner spinnerTaskName = view.findViewById(R.id.addTaskName);

        // Lấy danh sách tasks từ cơ sở dữ liệu
        List<Task> taskList = dbHelper.getAllTasks();
        List<String> taskNames = new ArrayList<>();
        for (Task task : taskList) {
            taskNames.add(task.getTaskName());
        }

        // Thiết lập adapter cho Spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, taskNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTaskName.setAdapter(spinnerAdapter);

        // Biến để lưu Task ID và Estimate Day
        int[] selectedTaskId = {0};

        // Lấy thông tin task được chọn
        spinnerTaskName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Task selectedTask = taskList.get(position);
                selectedTaskId[0] = selectedTask.getTaskID();
                etTaskId.setText(String.valueOf(selectedTask.getTaskID()));
                etEstimateDay.setText(String.valueOf(selectedTask.getEstimateDay()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                etTaskId.setText("");
                etEstimateDay.setText("");
            }
        });

        // Thiết lập DatePicker cho Start Date
        etStartDate.setOnClickListener(v -> showDateTimePicker(etStartDate));

        // Thiết lập DatePicker cho End Date
        etEndDate.setOnClickListener(v -> showDateTimePicker(etEndDate));

        builder.setView(view)
                .setTitle("Add Dev Task")
                .setPositiveButton("Add", (dialog, which) -> {
                    String devName = etDevName.getText().toString();
                    String startDate = etStartDate.getText().toString();
                    String endDate = etEndDate.getText().toString();
                    int taskId = selectedTaskId[0];
                    String taskName = taskNames.get(spinnerTaskName.getSelectedItemPosition());
                    int estimateDay = Integer.parseInt(etEstimateDay.getText().toString());

                    // Thêm task mới vào cơ sở dữ liệu
                    long result = dbHelper.insertDevTask(devName, taskId, startDate, endDate);

                    if (result != -1) { // Kiểm tra nếu thêm thành công
                        // Cập nhật danh sách devTaskList
                        devTaskList.add(new DevTask(result, devName, taskId, startDate, endDate, taskName, estimateDay));

                        // Cập nhật lại adapter của RecyclerView/ListView
                        adapter.notifyDataSetChanged();
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
