package com.example.project_management.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper{
    // Tên và phiên bản cơ sở dữ liệu
    private static final String DATABASE_NAME = "ProjectManagement.db";
    private static final int DATABASE_VERSION = 1;

    // Tên bảng
    private static final String TABLE_DEV_TASK = "dev_task";
    private static final String TABLE_TASK = "task";

    // Các cột của bảng dev_task
    private static final String COL_ID = "ID";
    private static final String COL_DEV_NAME = "DEV_NAME";
    private static final String COL_TASKID = "TASKID";
    private static final String COL_STARTDATE = "STARTDATE";
    private static final String COL_ENDDATE = "ENDDATE";

    // Các cột của bảng task
    private static final String COL_TASK_NAME = "TASK_NAME";
    private static final String COL_ESTIMATE_DAY = "ESTIMATE_DAY";

    // Câu lệnh SQL để tạo bảng dev_task
    private static final String CREATE_TABLE_DEV_TASK = "CREATE TABLE " + TABLE_DEV_TASK + "("
            + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_DEV_NAME + " TEXT NOT NULL, "
            + COL_TASKID + " INTEGER NOT NULL, "
            + COL_STARTDATE + " TEXT NOT NULL, "
            + COL_ENDDATE + " TEXT NOT NULL);";

    // Câu lệnh SQL để tạo bảng task
    private static final String CREATE_TABLE_TASK = "CREATE TABLE " + TABLE_TASK + "("
            + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_TASK_NAME + " TEXT NOT NULL, "
            + COL_ESTIMATE_DAY + " INTEGER NOT NULL);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo các bảng
        db.execSQL(CREATE_TABLE_DEV_TASK);
        db.execSQL(CREATE_TABLE_TASK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa bảng cũ nếu có (khi nâng cấp)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEV_TASK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK);
        onCreate(db); // Tạo lại bảng
    }

    // Phương thức thêm dữ liệu vào bảng dev_task
    public long insertDevTask(String devName, int taskId, String startDate, String endDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DEV_NAME, devName);
        values.put(COL_TASKID, taskId);
        values.put(COL_STARTDATE, startDate);
        values.put(COL_ENDDATE, endDate);
        return db.insert(TABLE_DEV_TASK, null, values);
    }

    // Phương thức thêm dữ liệu vào bảng task
    public long insertTask(String taskName, int estimateDay) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TASK_NAME, taskName);
        values.put(COL_ESTIMATE_DAY, estimateDay);
        return db.insert(TABLE_TASK, null, values);
    }

    // Phương thức để lấy dữ liệu từ bảng dev_task
    public Cursor getDevTasks() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_DEV_TASK, null);
    }

    // Phương thức để lấy dữ liệu từ bảng dev_task và sắp xếp theo TASKID
    public Cursor getDevTasksSortedByTaskID() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_DEV_TASK + " ORDER BY " + COL_TASKID;
        return db.rawQuery(query, null);
    }

    // Phương thức kiểm tra xem dữ liệu mẫu đã tồn tại hay chưa
    public boolean isDataPresent() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_DEV_TASK, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0; // Trả về true nếu có dữ liệu
    }

    // Phương thức để tạo dữ liệu mẫu
    public void insertSampleData() {
        if (!isDataPresent()) { // Kiểm tra nếu không có dữ liệu thì thêm mới
            // Thêm dữ liệu vào bảng task
            insertTask("Order list", 5);
            insertTask("Order detail", 3);
            insertTask("Product list", 3);
            insertTask("Product detail", 3);
            insertTask("Coupon list", 3);

            // Thêm dữ liệu vào bảng dev_task
            insertDevTask("Ramesh", 3, "2024/5/1", "2024/5/3");
            insertDevTask("Khilan", 2, "2024/5/2", "2024/5/4");
            insertDevTask("Kaushik", 1, "2024/4/28", "2024/4/30");
            insertDevTask("Kaushik", 4, "2024/5/6", "2024/5/8");
            insertDevTask("Superman", 5, "2024/5/3", "2024/5/5");
        }
    }


    // Phương thức để lấy dữ liệu từ bảng task
    public Cursor getTasks() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_TASK, null);
    }
}
