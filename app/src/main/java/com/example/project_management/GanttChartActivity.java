package com.example.project_management;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_management.Database.DatabaseHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            String chartStartDate = dbHelper.getEarliestStartDate(); // Lấy ngày bắt đầu nhỏ nhất từ DB
            // Kiểm tra và chuyển đổi định dạng nếu cần thiết
            if (chartStartDate != null) {
                // Chuyển đổi về định dạng dd/MM/yyyy hh:mm nếu cần
                chartStartDate = convertDateFormat(chartStartDate);
            }
            Log.d("đây là chartStartDate", chartStartDate);
            adapter = new GanttChartAdapter(devTaskList, chartStartDate);
            recyclerView.setAdapter(adapter);
        } else {
            // Thông báo nếu không có dữ liệu
            Toast.makeText(this, "No data available for Gantt Chart", Toast.LENGTH_SHORT).show();
        }
    }
    // Phương thức chuyển đổi định dạng nếu cần
    private String convertDateFormat(String date) {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            SimpleDateFormat desiredFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date originalDate = originalFormat.parse(date);
            return desiredFormat.format(originalDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return date; // Trả về ngày không đổi nếu có lỗi
        }
    }
}
