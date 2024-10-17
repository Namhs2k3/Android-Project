package com.example.project_management.Database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.project_management.DevTask;
import com.example.project_management.Task;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "project_management.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_DEV_TASK = "dev_task";
    public static final String TABLE_TASK = "task";

    public static final String COL_ID = "ID";
    public static final String COL_DEV_NAME = "DEV_NAME";
    public static final String COL_TASKID = "TASKID";
    public static final String COL_STARTDATE = "STARTDATE";
    public static final String COL_ENDDATE = "ENDDATE";

    // Columns for task table
    public static final String COL_TASK_NAME = "TASK_NAME";
    public static final String COL_ESTIMATE_DAY = "ESTIMATE_DAY";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create dev_task table
        String CREATE_TABLE_DEV_TASK = "CREATE TABLE " + TABLE_DEV_TASK + "("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_DEV_NAME + " TEXT NOT NULL, "
                + COL_TASKID + " INTEGER NOT NULL, "
                + COL_STARTDATE + " TEXT NOT NULL, "
                + COL_ENDDATE + " TEXT NOT NULL);";
        db.execSQL(CREATE_TABLE_DEV_TASK);

        // Create task table
        String CREATE_TABLE_TASK = "CREATE TABLE " + TABLE_TASK + "("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TASK_NAME + " TEXT NOT NULL, "
                + COL_ESTIMATE_DAY + " INTEGER NOT NULL);";
        db.execSQL(CREATE_TABLE_TASK);

        insertSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEV_TASK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK);
        onCreate(db);
    }

    public void insertSampleData(SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        // Check if the task already exists before inserting
        if (!isTaskExists(db, "Order List")) {
            values.put(COL_TASK_NAME, "Order List");
            values.put(COL_ESTIMATE_DAY, 5);
            db.insert(TABLE_TASK, null, values);
        }

        if (!isTaskExists(db, "Order detail")) {
            values.put(COL_TASK_NAME, "Order detail");
            values.put(COL_ESTIMATE_DAY, 3);
            db.insert(TABLE_TASK, null, values);
        }

        if (!isTaskExists(db, "Product list")) {
            values.put(COL_TASK_NAME, "Product list");
            values.put(COL_ESTIMATE_DAY, 3);
            db.insert(TABLE_TASK, null, values);
        }

        if (!isTaskExists(db, "Product detail")) {
            values.put(COL_TASK_NAME, "Product detail");
            values.put(COL_ESTIMATE_DAY, 3);
            db.insert(TABLE_TASK, null, values);
        }

        if (!isTaskExists(db, "Coupon list")) {
            values.put(COL_TASK_NAME, "Coupon list");
            values.put(COL_ESTIMATE_DAY, 3);
            db.insert(TABLE_TASK, null, values);
        }
    }


    public Cursor getDevTasksWithDetails() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + TABLE_DEV_TASK + ".*, " + TABLE_TASK + "." + COL_TASK_NAME + ", " + TABLE_TASK + "." + COL_ESTIMATE_DAY +
                " FROM " + TABLE_DEV_TASK +
                " JOIN " + TABLE_TASK + " ON " + TABLE_DEV_TASK + "." + COL_TASKID + " = " + TABLE_TASK + "." + COL_ID;
        return db.rawQuery(query, null);
    }

    public long insertDevTask(String devName, String taskName, String startDate, String endDate, int estimateDay) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        long taskID = insertTask(taskName, estimateDay);
        values.put(COL_DEV_NAME, devName);
        values.put(COL_TASKID, taskID);
        values.put(COL_STARTDATE, startDate);
        values.put(COL_ENDDATE, endDate);

        db.insert(TABLE_DEV_TASK, null, values);

        return taskID;
    }

    public long insertTask(String taskName, int estimateDay){
        SQLiteDatabase db = this.getWritableDatabase();

        // Kiểm tra nếu task đã tồn tại
        String query = "SELECT " + COL_ID + " FROM " + TABLE_TASK + " WHERE " + COL_TASK_NAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{taskName});

        if (cursor.moveToFirst()) {
            // Task đã tồn tại, trả về taskID
            long taskID = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
            cursor.close();
            return taskID;
        }

        // Nếu không tồn tại, thêm mới task
        ContentValues values = new ContentValues();
        values.put(COL_TASK_NAME, taskName);
        values.put(COL_ESTIMATE_DAY, estimateDay);

        long taskID = db.insert(TABLE_TASK, null, values);
        cursor.close();
        return taskID;
    }



    public void updateDevTask(long id, String devName, String taskName, String startDate, String endDate, int estimateDay, int taskID) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        ContentValues values2 = new ContentValues();

        values.put(COL_DEV_NAME, devName);
        values.put(COL_STARTDATE, startDate);
        values.put(COL_ENDDATE, endDate);

        values2.put(COL_TASK_NAME, taskName);
        values2.put(COL_ESTIMATE_DAY, estimateDay);

        db.update(TABLE_DEV_TASK, values, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.update(TABLE_TASK, values2,   "ID = ?", new String[]{String.valueOf(taskID)});

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
                return (int) (differenceInMillis / (1000 * 60 * 60 * 24)) +1; // Số ngày
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void deleteDevTask(int id, int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DEV_TASK, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.delete(TABLE_TASK, COL_ID + " = ?", new String[]{String.valueOf(taskId)});
    }

    public int getIdByTaskId(int taskId) {
        int id = -1; // Biến mặc định cho trường hợp không tìm thấy

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_DEV_TASK,                         // Bảng
                new String[]{COL_ID},                   // Cột muốn lấy
                COL_TASKID + " = ?",                   // Điều kiện WHERE
                new String[]{String.valueOf(taskId)},   // Giá trị của điều kiện
                null, null, null                        // Nhóm, bộ lọc, sắp xếp
        );

        if (cursor != null && cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)); // Lấy id của task
            cursor.close();
        }

        return id; // Trả về id, nếu không tìm thấy sẽ trả về -1
    }


    public Task getTaskById(int taskId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("task", null, "ID = ?", new String[]{String.valueOf(taskId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("ID"));
            String taskName = cursor.getString(cursor.getColumnIndexOrThrow("TASK_NAME"));
            int estimateDay = cursor.getInt(cursor.getColumnIndexOrThrow("ESTIMATE_DAY"));
            cursor.close();
            return new Task(id, taskName, estimateDay); // Giả định có constructor phù hợp
        }
        return null;
    }

    public List<Task> getAllTasks() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+ TABLE_TASK, null);

        if (cursor.moveToFirst()) {
            do {
                int taskID = cursor.getInt(cursor.getColumnIndexOrThrow("ID"));
                String taskName = cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_NAME));
                int estimateDay = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ESTIMATE_DAY));

                taskList.add(new Task(taskID, taskName, estimateDay));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return taskList;
    }

    @SuppressLint("Range")
    public String getEarliestStartDate() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT MIN(strftime('%Y-%m-%d %H:%M', substr(" + COL_STARTDATE + ", 7, 4) || '-' || substr(" + COL_STARTDATE + ", 4, 2) || '-' || substr(" + COL_STARTDATE + ", 1, 2) || ' ' || substr(" + COL_STARTDATE + ", 12))) AS EarliestStartDate FROM " + TABLE_DEV_TASK;
        Cursor cursor = db.rawQuery(query, null);
        String earliestStartDate = null;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                earliestStartDate = cursor.getString(cursor.getColumnIndex("EarliestStartDate"));
            }
            cursor.close();
        }
        db.close();
        return earliestStartDate;
    }
    public boolean isTaskExists(SQLiteDatabase db, String taskName) {
        String query = "SELECT 1 FROM " + TABLE_TASK + " WHERE " + COL_TASK_NAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{taskName});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public boolean isTaskNameExists(String taskName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_TASK + " WHERE LOWER(task_name) = ?";
        Cursor cursor = db.rawQuery(query, new String[]{taskName.toLowerCase()});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }


}
