package com.example.tovisit_anmariya_c0775497_android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "PlacesDatabase";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "favouritePlaces";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "NAME";
    public static final String COLUMN_VISITED = "VISITED";
    public static final String COLUMN_LAT = "LATITUDE";
    public static final String COLUMN_LNG = "LONGITUDE" ;

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "CREATE TABLE " + TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER NOT NULL CONSTRAINT employee_pk PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " varchar(200) NOT NULL, " +
                COLUMN_VISITED + " bool NOT NULL, " +
                COLUMN_LAT + " double(200) NOT NULL, " +
                COLUMN_LNG + " double NOT NULL);";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
        db.execSQL(sql);
        onCreate(db);
    }


    boolean addPlace(String name, Boolean isVisited, double lat, double lng){

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_VISITED, isVisited);
        cv.put(COLUMN_LAT, lat);
        cv.put(COLUMN_LNG, lng);

        return sqLiteDatabase.insert(TABLE_NAME, null, cv) != -1;
    }

    Cursor getAllPlaces(){

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        return sqLiteDatabase.rawQuery("SELECT * from " + TABLE_NAME, null);
    }


    boolean updatePlace(int id, String name, Boolean isVisited, double lat, double lng){

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_VISITED, isVisited);
        cv.put(COLUMN_LAT, lat);
        cv.put(COLUMN_LNG, lng);


        return sqLiteDatabase.update(TABLE_NAME, cv, COLUMN_ID + "=?", new String[]{String.valueOf(id)}) > 0 ;


    }

    int numberOfResults(double lat, double lng){

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        return sqLiteDatabase.rawQuery("SELECT * from " + TABLE_NAME + " WHERE " +
                COLUMN_LAT+ " =?" + " AND " + COLUMN_LNG + " =? ", new String[]{String.valueOf(lat),String.valueOf(lng)})
                .getCount();



    }

    boolean removePlace(int id){

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        return sqLiteDatabase.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)}) > 0 ;

    }
}
