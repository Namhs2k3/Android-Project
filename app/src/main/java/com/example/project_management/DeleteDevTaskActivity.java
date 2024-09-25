package com.example.project_management;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
                int taskId = cursor.getInt(cursor.getColumnIndexOrThrow("ID"));
                String taskName = cursor.getString(cursor.getColumnIndexOrThrow("TASK_NAME"));// Lấy task ID
                String devName = cursor.getString(cursor.getColumnIndexOrThrow("DEV_NAME")); // Lấy tên

                // Thêm vào danh sách với ID và tên
                devTaskList.add(new DevTask(taskId, taskName, devName)); // Sử dụng constructor mới
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        // Sắp xếp danh sách nếu cần (tùy chọn)
        devTaskList.sort(Comparator.comparing(DevTask::getDevName)); // Giả sử DevTask có phương thức getDevName()
    }


}
