package com.example.project_management;

import android.database.Cursor;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_management.Database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DevTaskAdapter adapter;
    private List<DevTask> devTaskList;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewDevTask);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DatabaseHelper(this);

        // Thêm dữ liệu mẫu nếu cần
        dbHelper.insertSampleData();

        // Lấy danh sách dev_task từ cơ sở dữ liệu và sắp xếp theo Task ID
        loadDevTasks();

        adapter = new DevTaskAdapter(devTaskList);
        recyclerView.setAdapter(adapter);
    }

    private void loadDevTasks() {
        devTaskList = new ArrayList<>();
        Cursor cursor = dbHelper.getDevTasks();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("ID"));
                String devName = cursor.getString(cursor.getColumnIndexOrThrow("DEV_NAME"));
                int taskId = cursor.getInt(cursor.getColumnIndexOrThrow("TASKID"));
                String startDate = cursor.getString(cursor.getColumnIndexOrThrow("STARTDATE"));
                String endDate = cursor.getString(cursor.getColumnIndexOrThrow("ENDDATE"));

                // Lấy TASK_NAME và ESTIMATE_DAY từ bảng task
                Cursor taskCursor = dbHelper.getTasks(); // Lấy tất cả các task
                String taskName = "";
                int estimateDay = 0;
                while (taskCursor.moveToNext()) {
                    if (taskCursor.getInt(taskCursor.getColumnIndexOrThrow("ID")) == taskId) {
                        taskName = taskCursor.getString(taskCursor.getColumnIndexOrThrow("TASK_NAME"));
                        estimateDay = taskCursor.getInt(taskCursor.getColumnIndexOrThrow("ESTIMATE_DAY"));
                        break;
                    }
                }
                taskCursor.close();

                // Thêm vào danh sách
                devTaskList.add(new DevTask(id, devName, taskId, startDate, endDate,taskName, estimateDay));
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        // Sắp xếp danh sách theo Task ID
        devTaskList.sort((task1, task2) -> Integer.compare(task1.getTaskId(), task2.getTaskId()));
    }
}
