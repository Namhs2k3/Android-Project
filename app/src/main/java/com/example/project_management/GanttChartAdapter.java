package com.example.project_management;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GanttChartAdapter extends RecyclerView.Adapter<GanttChartAdapter.ViewHolder> {
    private List<DevTask> devTaskList;
    private String chartStartDate; // Ngày bắt đầu của biểu đồ Gantt

    public GanttChartAdapter(List<DevTask> devTaskList, String chartStartDate) {
        this.devTaskList = devTaskList;
        this.chartStartDate = chartStartDate; // Khởi tạo ngày bắt đầu của Gantt Chart
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
        holder.devNameTextView.setText(devTask.getDevName());
        holder.taskName.setText(devTask.getTaskName());

        // Tính toán chiều rộng và vị trí bắt đầu của thanh biểu đồ Gantt
        try {
            // Tính toán số ngày từ ngày bắt đầu biểu đồ Gantt đến ngày bắt đầu của task
            long daysFromChartStartToTaskStart = calculateDuration(chartStartDate, devTask.getStartDate());
            long taskDuration = calculateDuration(devTask.getStartDate(), devTask.getEndDate());

            // Chiều rộng của thanh Gantt (10dp cho mỗi ngày)
            int width = (int) (taskDuration * 10);
            // Tính toán vị trí bắt đầu của thanh Gantt
            int marginStart = (int) (daysFromChartStartToTaskStart * 10);

            View ganttBar = holder.itemView.findViewById(R.id.ganttBar);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) ganttBar.getLayoutParams();
            params.width = width; // Thiết lập chiều rộng
            params.setMarginStart(marginStart); // Thiết lập vị trí bắt đầu (marginStart)
            ganttBar.setLayoutParams(params); // Cập nhật LayoutParams
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return devTaskList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView devNameTextView;
        TextView taskName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            devNameTextView = itemView.findViewById(R.id.devName);
            taskName = itemView.findViewById(R.id.taskName);
        }
    }

    // Phương thức để tính toán số ngày giữa hai ngày
    private long calculateDuration(String startDateStr, String endDateStr) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.getDefault()); // Định dạng ngày
        Date startDate = dateFormat.parse(startDateStr);
        Date endDate = dateFormat.parse(endDateStr);

        // Tính số ngày giữa startDate và endDate
        return (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24); // Chuyển đổi từ milliseconds sang days
    }

}
