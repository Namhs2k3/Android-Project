package com.example.project_management;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GanttChartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GanttChartAdapter adapter;
    private List<DevTask> devTaskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gantt_chart);

        recyclerView = findViewById(R.id.recyclerViewGantt);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        devTaskList = intent.getParcelableArrayListExtra("devTaskList");

        if (devTaskList != null && !devTaskList.isEmpty()) {
            // Tạo adapter và thiết lập cho RecyclerView
            String chartStartDate = "16/09/2024 00:00"; // Ngày bắt đầu của biểu đồ Gantt
            adapter = new GanttChartAdapter(devTaskList, chartStartDate);
            recyclerView.setAdapter(adapter);
        } else {
            // Thông báo nếu không có dữ liệu
            Toast.makeText(this, "No data available for Gantt Chart", Toast.LENGTH_SHORT).show();
        }
    }
}
