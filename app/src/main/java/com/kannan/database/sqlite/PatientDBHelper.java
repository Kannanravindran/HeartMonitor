package com.kannan.database.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.kannan.Bean.AccelEntryBean;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by mldof on 3/3/2016.
 */
public class PatientDBHelper extends SQLiteOpenHelper{
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME="patient.db";

    private String name;
    private int id;
    private int age;
    private String sex;
    private String tableName;

    public PatientDBHelper(Context context, String patientName, int patientId, int patientAge, String patientSex) {
        super(context, DATABASE_NAME, null, 1);
        name = patientName;
        id = patientId;
        age = patientAge;
        sex = patientSex;
        tableName = name + "_" + id + "_" + age + "_" + sex;
    }

    // run query to create the table
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("Creating table ", tableName);
        String CREATE_PATIENT_TABLE = "CREATE TABLE " +
                tableName +
                "( createdAt DATETIME," +
                  "x INTEGER, " +
                  "y INTEGER, " +
                  "z INTEGER )";

        db.execSQL(CREATE_PATIENT_TABLE);
    }


    public void addEntry(AccelEntryBean newEntry) {
        Log.d("Adding entry  ", newEntry.toString());
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        values.put("createdAt", dateFormat.format(date)); // insert at current time
        values.put("x", newEntry.getX());
        values.put("y", newEntry.getY());
        values.put("z", newEntry.getZ());

        db.insert(tableName, null, values);
    }

    public List<AccelEntryBean> getAllEntries() throws SQLiteException {

        String SELECT_TOP10 = "SELECT * FROM " + tableName + " ORDER BY datetime(createdAt) DESC LIMIT 10";
        List<AccelEntryBean> entriesToReturn = new ArrayList<AccelEntryBean>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(SELECT_TOP10, null);

        AccelEntryBean entry = null;
        if (cursor.moveToFirst()) {
            do {
                entry = new AccelEntryBean();
                entry.setTimestamp(Timestamp.valueOf(cursor.getString(0)));
                entry.setX(Float.parseFloat(cursor.getString(1)));
                entry.setY(Float.parseFloat(cursor.getString(2)));
                entry.setZ(Float.parseFloat(cursor.getString(3)));

                // Add to list
                entriesToReturn.add(entry);
            } while (cursor.moveToNext());
        }
        Log.d("Pulling all entries  ", entriesToReturn.toString());
        return entriesToReturn;
    }

    public void createTableForPatient() {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.d("Creating table ", tableName);
        String CREATE_PATIENT_TABLE = "CREATE TABLE " +
                tableName +
                "( createdAt DATETIME," +
                "x INTEGER, " +
                "y INTEGER, " +
                "z INTEGER )";

        db.execSQL(CREATE_PATIENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
