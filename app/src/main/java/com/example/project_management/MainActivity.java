package com.example.project_management;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;

import android.widget.Button;
import android.widget.EditText;

import android.widget.Toast;

import androidx.appcompat.widget.SearchView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_management.Database.DatabaseHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

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

        // Nút Settings
        ImageButton settingsButton = (ImageButton) findViewById(R.id.menu_settings);
        settingsButton.setOnClickListener(v -> showSettingsMenu()); // Gọi showSettingsMenu()

//        // Load the preference in onCreate to apply it immediately
//        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
//        boolean isEstimateDayVisible = preferences.getBoolean("showEstimateDay", false);
//
//        // Pass this value to the adapter initially
//        adapter.setShowEstimateDay(isEstimateDayVisible);

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


    }



    private void showAddDevTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_dev_task, null);

        EditText etDevName = view.findViewById(R.id.addDevName);
        EditText etTaskName = view.findViewById(R.id.addTaskName);
        EditText etStartDate = view.findViewById(R.id.addStartDate);
        EditText etEndDate = view.findViewById(R.id.addEndDate);
        EditText etEstimateDay = view.findViewById(R.id.addEstimateDay);


        AtomicInteger estimateDay = new AtomicInteger();
        // Thiết lập DatePicker cho Start Date
        etStartDate.setOnClickListener(v -> showDateTimePicker(etStartDate, () -> {
            if (isStartDateAndEndDateHasValue(etStartDate.getText().toString().trim(), etEndDate.getText().toString().trim())) {
                etEstimateDay.setEnabled(false); // Disable khi cả Start và End date đều có giá trị
                updateEstimateDay(etStartDate, etEndDate, etEstimateDay);
            } else {
                if (isStartDateAndEndDateEmpty(etStartDate.getText().toString().trim(), etEndDate.getText().toString().trim())) {
                    etEstimateDay.setEnabled(true); // Enable lại khi cả hai đều trống
                }
            }
        }));

        etEndDate.setOnClickListener(v -> showDateTimePicker(etEndDate, () -> {
            if (isStartDateAndEndDateHasValue(etStartDate.getText().toString().trim(), etEndDate.getText().toString().trim())) {
                etEstimateDay.setEnabled(false); // Disable khi cả Start và End date đều có giá trị
                updateEstimateDay(etStartDate, etEndDate, etEstimateDay);
            } else {
                if (isStartDateAndEndDateEmpty(etStartDate.getText().toString().trim(), etEndDate.getText().toString().trim())) {
                    etEstimateDay.setEnabled(true); // Enable lại khi cả hai đều trống
                }
            }
        }));

        // Lắng nghe sự thay đổi trên Start Date và End Date
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isStartDateAndEndDateEmpty(etStartDate.getText().toString().trim(), etEndDate.getText().toString().trim())) {
                    etEstimateDay.setEnabled(true); // Bật ô nhập Estimate Day
                } else {
                    etEstimateDay.setEnabled(false); // Tắt ô nhập Estimate Day
                }
            }
        };

        // Gán TextWatcher cho cả hai trường
        etStartDate.addTextChangedListener(textWatcher);
        etEndDate.addTextChangedListener(textWatcher);

        builder.setView(view)
                .setTitle("Add Dev Task")
                .setPositiveButton("Add", (dialog, which) -> {
                    String devName = etDevName.getText().toString();
                    String taskName = etTaskName.getText().toString().trim();
                    String startDate = etStartDate.getText().toString();
                    String endDate = etEndDate.getText().toString();

                    Log.d("TAG", "startDate in add dialog: " + startDate);
                    Log.d("TAG", "endDate in add dialog: " + endDate);

                    if(!isTaskNameAndDevNameValid(taskName, devName)){
                        Toast.makeText(this, "Both Task name and Dev name can not be null", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Kiểm tra trùng tên task
                    if (dbHelper.isTaskNameExists(taskName)) {
                        Toast.makeText(this, "Task name already exists!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!isStartDateAndEndDateHasValue(startDate, endDate)) {
                        if(isStartDateAndEndDateEmpty(startDate, endDate))
                            if(etEstimateDay.getText().length() >0)
                                estimateDay.set(Integer.parseInt(etEstimateDay.getText().toString()));
                            else
                                estimateDay.set(0);
                        else {
                            Toast.makeText(this, "Both Start Date and End Date must either be both empty or both have values. If one has a value, the other must also have a value.", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }else
                        estimateDay.set(Integer.parseInt(etEstimateDay.getText().toString()));

                    if (!isValidDateRange(startDate, endDate)) {
                        Toast.makeText(this, "End Date must be greater than or equal to Start Date.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Thêm task mới vào cơ sở dữ liệu
                    long result = dbHelper.insertDevTask(devName, taskName, startDate, endDate, estimateDay.get());

                    if (result != -1) { // Kiểm tra nếu thêm thành công
                        // Lấy taskId từ result
                        int taskId = (int) result; // Giả sử taskId được lấy từ result
                        // Cập nhật danh sách devTaskList
                        devTaskList.add(new DevTask(result, devName, taskName, startDate, endDate, estimateDay.get(), taskId)); // Cập nhật constructor
                        // Cập nhật lại adapter của RecyclerView/ListView
                        adapter.notifyDataSetChanged();
                    }
                    Toast.makeText(this, "Add Dev Task Successfully!", Toast.LENGTH_LONG).show();

                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public boolean isStartDateAndEndDateHasValue(String startDate, String endDate) {
        return (!startDate.isEmpty() && !endDate.isEmpty());
    }
    public boolean isStartDateAndEndDateEmpty(String startDate, String endDate) {
        return (startDate.isEmpty() && endDate.isEmpty());
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

        Log.d("TAG", "startDateStr: "+startDateStr);
        Log.d("TAG", "endDateStr: "+endDateStr);
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Log.e("TAG", "hihi: " );
            Date startDate = dateFormat.parse(startDateStr);
            Log.e("TAG", "hihi1: " );
            Date endDate = dateFormat.parse(endDateStr);
            Log.d("TAG", "startDate: "+startDate);
            Log.d("TAG", "endDate: "+endDate);
            return startDate != null && endDate != null && !startDate.after(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("TAG", "Lỗi: "+e.getMessage());
            return false;
        }
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

    private void showDateTimePicker(EditText editText, Runnable onDateSet) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timeView, selectedHour, selectedMinute) -> {
                String dateTime = String.format("%02d/%02d/%04d %02d:%02d", selectedDay, selectedMonth + 1, selectedYear, selectedHour, selectedMinute);
                editText.setText(dateTime); // Cập nhật EditText với ngày giờ đã chọn

                // Gọi callback để cập nhật estimateDay
                onDateSet.run();
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
                long id = cursor.getLong(cursor.getColumnIndexOrThrow("ID"));
                String devName = cursor.getString(cursor.getColumnIndexOrThrow("DEV_NAME"));
                int taskId = cursor.getInt(cursor.getColumnIndexOrThrow("TASKID"));
                String startDate = cursor.getString(cursor.getColumnIndexOrThrow("STARTDATE"));
                String endDate = cursor.getString(cursor.getColumnIndexOrThrow("ENDDATE"));
                String taskName = cursor.getString(cursor.getColumnIndexOrThrow("TASK_NAME"));
                int estimateDay = cursor.getInt(cursor.getColumnIndexOrThrow("ESTIMATE_DAY"));

                // Thêm vào danh sách
                devTaskList.add(new DevTask(id, devName, taskName, startDate, endDate, estimateDay, taskId));
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        // Sắp xếp danh sách theo Task ID
        devTaskList.sort((task1, task2) -> Integer.compare(task1.getTaskId(), task2.getTaskId()));
    }

    private void showSettingsMenu() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // Không cần tiêu đề
        dialog.setContentView(R.layout.dialog_settings_menu);

        // Đặt dialog toàn màn hình
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }

        // Ánh xạ các button trong dialog
        ImageButton btnBack = dialog.findViewById(R.id.btnBack);
        Button btnDeleteTask = dialog.findViewById(R.id.btn_delete_task);
        Button btnGanttChart = dialog.findViewById(R.id.btn_gantt_chart);
        Switch switchEstimateDay = dialog.findViewById(R.id.switch_estimate_day);


        btnBack.setOnClickListener(v -> dialog.dismiss());
        // Sự kiện khi click vào "Delete Task"
        btnDeleteTask.setOnClickListener(v -> {
            // Hiển thị layout activity_delete_task
            Intent intent = new Intent(MainActivity.this, DeleteDevTaskActivity.class);
            startActivity(intent);
            dialog.dismiss(); // Đóng dialog
        });

        // Sự kiện khi click vào "Another Action"
        btnGanttChart.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GanttChartActivity.class);

            // Chuyển danh sách devTaskList qua Intent
            intent.putParcelableArrayListExtra("devTaskList", (ArrayList<? extends Parcelable>) devTaskList);

            startActivity(intent);
        });

        // Load the current preference from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isEstimateDayVisible = preferences.getBoolean("showEstimateDay", true); // default to true
        switchEstimateDay.setChecked(isEstimateDayVisible); // Set the switch state


        // Handle switch toggle event
        switchEstimateDay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("showEstimateDay", isChecked);
            editor.apply();
            // Update the adapter and notify it to show/hide the Estimate Day
            adapter.setShowEstimateDay(isChecked); // You'll need to add this method to your adapter
        });

        // Hiển thị dialog
        dialog.show();
    }
}
