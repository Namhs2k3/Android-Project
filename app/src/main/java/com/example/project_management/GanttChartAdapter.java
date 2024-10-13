package com.example.project_management;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

public class GanttChartAdapter extends RecyclerView.Adapter<GanttChartAdapter.ViewHolder> {
    private List<DevTask> devTaskList;
    private String chartStartDate; // Ngày bắt đầu của biểu đồ Gantt
    private String weekStart; // Ngày bắt đầu của tuần để tính toán

    public GanttChartAdapter(List<DevTask> devTaskList, String chartStartDate, String weekStart) {
        this.devTaskList = devTaskList;
        this.chartStartDate = chartStartDate; // Khởi tạo ngày bắt đầu của Gantt Chart
        this.weekStart = weekStart; // Khởi tạo ngày bắt đầu của tuần
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gantt_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DevTask devTask = devTaskList.get(position);
        holder.taskName.setText(devTask.getTaskName());
        holder.devNameTextView.setText(devTask.getDevName());

        // Sử dụng LocalDateTime với định dạng ngày và giờ
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            LocalDateTime startDate;
            LocalDateTime endDate;

            try {
                // Phân tích ngày bắt đầu và ngày kết thúc với cả ngày và giờ
                startDate = LocalDateTime.parse(devTask.getStartDate(), dateTimeFormatter);
                endDate = LocalDateTime.parse(devTask.getEndDate(), dateTimeFormatter);
            } catch (DateTimeParseException e) {
                Log.e("GanttChartLog", "Date parsing error: " + e.getMessage());
                return;
            }

            // Tính toán tuần bắt đầu cho biểu đồ Gantt
            LocalDateTime weekStartDate = LocalDateTime.parse(chartStartDate, dateTimeFormatter);

            // Tính toán và hiển thị thanh Gantt
            holder.itemView.post(() -> {
                long totalDays = ChronoUnit.DAYS.between(weekStartDate.toLocalDate(), weekStartDate.plusDays(6).toLocalDate()) + 1;
                long startDaysOffset = ChronoUnit.DAYS.between(weekStartDate.toLocalDate(), startDate.toLocalDate());
                long endDaysOffset = ChronoUnit.DAYS.between(weekStartDate.toLocalDate(), endDate.toLocalDate());

                int itemViewWidth = holder.itemView.getWidth();
                ViewGroup.LayoutParams params = holder.ganttBar.getLayoutParams();

                // Thiết lập chiều rộng của thanh Gantt
                params.width = (int) ((endDaysOffset - startDaysOffset + 1) * (itemViewWidth / totalDays));
                holder.ganttBar.setLayoutParams(params);

                // Thiết lập vị trí của thanh Gantt
                holder.ganttBar.setTranslationX((int) (startDaysOffset * (itemViewWidth / totalDays)));
            });

        } catch (DateTimeParseException e) {
            Log.e("GanttChartLog", "Date parsing error: " + e.getMessage());
        }
    }


    @Override
    public int getItemCount() {
        return devTaskList.size();
    }

    public void setWeekStart(String weekStart) {
        this.weekStart = weekStart;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskName, devNameTextView;
        View ganttBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.taskName);
            devNameTextView = itemView.findViewById(R.id.devName);
            ganttBar = itemView.findViewById(R.id.ganttBar);
        }
    }
}
