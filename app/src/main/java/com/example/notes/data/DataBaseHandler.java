package com.example.notes.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.notes.data.dto.Note;

public class DataBaseHandler extends SQLiteOpenHelper {

    //Database version
    private static final int DATABASE_VERSION = 1;

    //Database name
    private static final String DATABASE_NAME = "notes.db";

    //Table name
    private static final String TABLE_NAME = "notedata";

    //Table fields
    private static final String CONTENT = "content";
    private static final String DATE = "date";
    private static final String TIME = "timeInMillis";
    private static final String ID = "ID";

    //Pagination
    private static int offset = 0;
    private static final int AMOUNT = 20;

    SQLiteDatabase database;

    public DataBaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + CONTENT + " TEXT, "
                + DATE + " TEXT, " + TIME + " BIGINT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addNote(Note note) {
        String content = note.getContent();
        String date = note.getDate().toString();
        long time = note.getTimeInMillis();

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONTENT, content);
        contentValues.put(DATE, date);
        contentValues.put(TIME, time);

        long result = db.insert(TABLE_NAME, null, contentValues);

        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public Cursor getData() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public Cursor getItemId(long timeInMillis) {
        SQLiteDatabase database = this.getWritableDatabase();
        String query = "SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + TIME + " = '" + timeInMillis + "'";
        Cursor data = database.rawQuery(query, null);
        return data;
    }

    public void updateNote(String newContent, int id, String oldContent, long timeInMillis, String date) {
        SQLiteDatabase database = this.getWritableDatabase();
        String query = "UPDATE " + TABLE_NAME + " SET " + CONTENT + " = '" +
                newContent + "', " + DATE + " = '" + date + "', " + TIME + " = '" + timeInMillis + "' WHERE " + ID + " = '" + id + "'" + " AND " + CONTENT + " = '" + oldContent + "'";
        database.execSQL(query);
    }

    public void deleteNote(int id, String content) {
        SQLiteDatabase database = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE " + ID + " = '" + id + "'" + " AND " + CONTENT
                + " = '" + content + "'";
        database.execSQL(query);
    }

    public Cursor sortFromNewToOld() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + TIME + " DESC " + "LIMIT '" + AMOUNT + "' OFFSET '" + offset + "'";
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public Cursor sortFromOldToNew() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + TIME + " ASC " + "LIMIT '" + AMOUNT + "' OFFSET '" + offset + "'";
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public void plusOffSet() {
        offset += 20;
    }

    public void setOffset(){
        offset = 0;
    }
}
