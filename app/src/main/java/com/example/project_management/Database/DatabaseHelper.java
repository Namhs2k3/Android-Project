package com.example.project_management.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.project_management.DevTask;
import com.example.project_management.Task;

import java.util.ArrayList;
import java.util.List;

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
        values.put(COL_TASK_NAME, "Order List");
        values.put(COL_ESTIMATE_DAY, 5);
        db.insert(TABLE_TASK, null, values);

        values.put(COL_TASK_NAME, "Order detail");
        values.put(COL_ESTIMATE_DAY, 3);
        db.insert(TABLE_TASK, null, values);

        values.put(COL_TASK_NAME, "Product list");
        values.put(COL_ESTIMATE_DAY, 3);
        db.insert(TABLE_TASK, null, values);

        values.put(COL_TASK_NAME, "Product detail");
        values.put(COL_ESTIMATE_DAY, 3);
        db.insert(TABLE_TASK, null, values);

        values.put(COL_TASK_NAME, "Coupon list");
        values.put(COL_ESTIMATE_DAY, 3);
        db.insert(TABLE_TASK, null, values);
    }

    public Cursor getDevTasksWithDetails() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + TABLE_DEV_TASK + ".*, " + TABLE_TASK + "." + COL_TASK_NAME + ", " + TABLE_TASK + "." + COL_ESTIMATE_DAY +
                " FROM " + TABLE_DEV_TASK +
                " JOIN " + TABLE_TASK + " ON " + TABLE_DEV_TASK + "." + COL_TASKID + " = " + TABLE_TASK + "." + COL_ID;
        return db.rawQuery(query, null);
    }

    public long insertDevTask(String devName, long taskId, String startDate, String endDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DEV_NAME, devName);
        values.put(COL_TASKID, taskId);
        values.put(COL_STARTDATE, startDate);
        values.put(COL_ENDDATE, endDate);
        return db.insert(TABLE_DEV_TASK, null, values);
    }

    public void updateDevTask(long id, String devName, long taskId, String startDate, String endDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DEV_NAME, devName);
        values.put(COL_TASKID, taskId);
        values.put(COL_STARTDATE, startDate);
        values.put(COL_ENDDATE, endDate);
        db.update(TABLE_DEV_TASK, values, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void deleteDevTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DEV_TASK, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void deleteTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASK, COL_ID + " = ?", new String[]{String.valueOf(taskId)});
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

    public List<DevTask> getAllDevTasks() {
        List<DevTask> devTaskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = getDevTasksWithDetails();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                String devName = cursor.getString(cursor.getColumnIndexOrThrow(COL_DEV_NAME));
                int taskId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASKID));
                String startDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_STARTDATE));
                int estimateDay = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ESTIMATE_DAY));
                String taskName = cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_NAME));
                String endDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_ENDDATE));

                // Create DevTask object and add to the list
                DevTask devTask = new DevTask(id, devName, taskId, startDate, endDate, taskName, estimateDay);
                devTaskList.add(devTask);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return devTaskList;
    }


}
