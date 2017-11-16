package com.rudainc.christmastree.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.rudainc.christmastree.provider.ChristmasTreeContract.*;


public class ChristmasTreeDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "christmas.db";
    private static final int DATABASE_VERSION = 1;

    public ChristmasTreeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_TABLE = "CREATE TABLE " + TreeEntry.TABLE_NAME + " (" +
                TreeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TreeEntry.COLUMN_CREATED_AT + " TIMESTAMP NOT NULL, " +
                TreeEntry.COLUMN_WATERED_AT + " TIMESTAMP NOT NULL)";

        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TreeEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
