package com.example.project_management;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_management.Database.DatabaseHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DeleteDevTaskActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DevTaskAdapter2 devTaskAdapter;
    private DatabaseHelper databaseHelper;
    private List<DevTask> devTaskList;
    private List<DevTask> selectedDevTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_task);  // Đảm bảo layout này đã được cập nhật đúng

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Khởi tạo devTaskList
        devTaskList = new ArrayList<>();
        Log.d("MainActivity", "devTaskList initialized");

        // Thêm dữ liệu mẫu nếu cần thiết
        databaseHelper.insertSampleData(databaseHelper.getWritableDatabase());

        // Lấy danh sách dev_task từ cơ sở dữ liệu
        loadDevTasks();
        Log.d("DeleteDevTaskActivity", "Tasks loaded: " + devTaskList.size());

        // Khởi tạo adapter
        devTaskAdapter = new DevTaskAdapter2(devTaskList, new ArrayList<>(devTaskList), this, databaseHelper);
        recyclerView.setAdapter(devTaskAdapter);
        Log.d("DeleteDevTaskActivity", "Adapter initialized");

        // Set up delete button click listener
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            // Quay lại màn hình trước đó
            // finish();
            // Hoặc chuyển đến MainActivity
            Intent intent1 = new Intent(DeleteDevTaskActivity.this, MainActivity.class);
            startActivity(intent1);
        });
        Button deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> {
            devTaskAdapter.deleteSelectedTasks(); // Gọi phương thức xóa
        });

    }

    private void loadDevTasks() {
        devTaskList = new ArrayList<>();
        Cursor cursor = databaseHelper.getDevTasksWithDetails();

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


}
