package com.example.project_management;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project_management.Database.DatabaseHelper;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GanttChartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GanttChartAdapter adapter;
    private List<DevTask> devTaskList;
    private int weekOffset = 0; // Biến để xác định tuần hiện tại

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
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            String chartStartDate = dbHelper.getEarliestStartDate();
            if (chartStartDate != null) {
                chartStartDate = convertDateFormat(chartStartDate);
            }

            // Hiển thị tuần đầu tiên
            displayWeek(chartStartDate);

            final String chartStartDateFinal = chartStartDate; // Tạo biến final
            // Nút để quay lại tuần trước
            Button btnPrevWeek = findViewById(R.id.btnPrevWeek);
            btnPrevWeek.setOnClickListener(v -> {
                weekOffset -= 1;
                displayWeek(chartStartDateFinal); // Sử dụng biến final
            });

            // Nút để chuyển đến tuần tiếp theo
            Button btnNextWeek = findViewById(R.id.btnNextWeek);
            btnNextWeek.setOnClickListener(v -> {
                weekOffset += 1;
                displayWeek(chartStartDateFinal); // Sử dụng biến final
            });
        } else {
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

    private void displayWeek(String chartStartDate) {
        // Lấy danh sách TextView của thanh Date
        TextView[] dateViews = new TextView[]{
                findViewById(R.id.date1), findViewById(R.id.date2),
                findViewById(R.id.date3), findViewById(R.id.date4),
                findViewById(R.id.date5), findViewById(R.id.date6),
                findViewById(R.id.date7)
        };

        // Tính toán ngày bắt đầu cho tuần hiện tại
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar startCalendar = Calendar.getInstance();
        try {
            Date startDate = dateFormat.parse(chartStartDate);
            startCalendar.setTime(startDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Cộng thêm weekOffset
        startCalendar.add(Calendar.WEEK_OF_YEAR, weekOffset);

        // Hiển thị các ngày của tuần
        for (int i = 0; i < 7; i++) {
            dateViews[i].setText(dateFormat.format(startCalendar.getTime()));
            startCalendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Cập nhật dữ liệu biểu đồ Gantt cho tuần này
        Calendar currentWeekStart = (Calendar) startCalendar.clone();
        currentWeekStart.add(Calendar.DAY_OF_YEAR, -7);  // Trừ lại 7 ngày vì đã cộng trong vòng lặp

        String weekStart = dateFormat.format(currentWeekStart.getTime());
        Log.d("GanttChartLogWeek", "Current week start: " + weekStart);
        Log.d("GanttChartLogWeek", "Current chart start: " + chartStartDate);

        // Sử dụng LocalDateTime với định dạng chỉ có ngày
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());

        // Chuyển đổi từ chuỗi ngày thành LocalDate (vì weekStart chỉ có ngày)
        LocalDate localDate = LocalDate.parse(weekStart, dateTimeFormatter);
        String formattedDate = localDate.format(dateTimeFormatter);
        Log.d("localDate", "localDate: "+formattedDate);

        // Thêm giờ mặc định (00:00) để tạo LocalDateTime
        LocalDateTime localDateTime = localDate.atTime(0, 0);
        DateTimeFormatter dateTimeOutputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault());
        String formattedLocalDateTime = localDateTime.format(dateTimeOutputFormatter);
        Log.d("localDateTime", "localDateTime: "+formattedLocalDateTime);

        // Nếu adapter đã khởi tạo trước đó, chỉ cần cập nhật dữ liệu
        if (adapter != null) {
            adapter = new GanttChartAdapter(devTaskList, formattedLocalDateTime.replace("T"," ") , weekStart);
            recyclerView.setAdapter(adapter);
        } else {
            adapter = new GanttChartAdapter(devTaskList, chartStartDate, weekStart);
            recyclerView.setAdapter(adapter);
        }
    }
}
