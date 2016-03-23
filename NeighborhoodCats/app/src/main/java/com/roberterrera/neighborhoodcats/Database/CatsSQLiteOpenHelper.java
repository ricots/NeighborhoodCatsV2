package com.roberterrera.neighborhoodcats.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.roberterrera.neighborhoodcats.Classes.Cat;

/**
 * Created by Rob on 3/21/16.
 */
public class CatsSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = CatsSQLiteOpenHelper.class.getCanonicalName();

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "NEIGHBORHOODCATS.db";
    public static final String CAT_LIST_TABLE_NAME = "YOUR_CATS";

    public static final String COL_ID = "_id";
    public static final String COL_NAME = "CAT_NAME";
    public static final String COL_DESC = "DESCRIPTION";
    public static final String COL_LOCATION = "LOCATION";
    public static final String COL_IMG = "IMAGE_PATH";
//    public static final String COL_THUMB = "THUMBNAIL_PATH";
    public static final String[] CATS_COLUMNS = {COL_ID,COL_NAME,COL_DESC, COL_LOCATION, COL_IMG};

    public CatsSQLiteOpenHelper (Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    private static final String CREATE_CAT_LIST_TABLE =
            "CREATE TABLE " + CAT_LIST_TABLE_NAME +
                    "(" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_NAME + " TEXT, " +
                    COL_DESC + " TEXT, " +
                    COL_LOCATION + " TEXT, " +
                    COL_IMG + " TEXT )";

    private static CatsSQLiteOpenHelper instance;
    public static CatsSQLiteOpenHelper getInstance(Context context) {
        if(instance == null){
            instance = new CatsSQLiteOpenHelper(context);
        }
        return instance;
    }

    public CatsSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CAT_LIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CAT_LIST_TABLE_NAME);
        this.onCreate(db);
    }

    public Cursor getCatsList() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(CAT_LIST_TABLE_NAME,
                CATS_COLUMNS,
                null,
                null,
                null,
                null,
                null,
                null );

//        cursor.close();
        return cursor;
    }

    public String getCatNameByID(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(CAT_LIST_TABLE_NAME,
                new String[]{COL_NAME},
                COL_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null,
                null );

        cursor.moveToFirst();
//        cursor.close();
        return cursor.getString(cursor.getColumnIndex(COL_LOCATION));
    }


    public void deleteCatByID(int id){
        SQLiteDatabase db = getWritableDatabase();

        String selection = "_id = ?";
        String[] selectionArgs = { String.valueOf(COL_ID) };

        db.delete(CAT_LIST_TABLE_NAME,
                selection,
                selectionArgs);
    }

    public void insert(int id, String name, String desc, String location, String photo){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_ID, id);
        values.put(COL_NAME, name);
        values.put(COL_DESC, desc);
        values.put(COL_LOCATION, location);
        values.put(COL_IMG, photo);

        db.insert(CAT_LIST_TABLE_NAME, null, values);

    }

    public String getLocationByID(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(CAT_LIST_TABLE_NAME,
                new String[]{COL_LOCATION},
                COL_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null,
                null );

        cursor.moveToFirst();
//        cursor.close();
        return cursor.getString(cursor.getColumnIndex(COL_LOCATION));
    }

    public Cat getCatByID(int id, String name, String desc, String photo, String location){
        SQLiteDatabase db = getReadableDatabase();

        String[] columns = CATS_COLUMNS;

        String selection = "id = ?";
        String[] selectionArgs = new String[]{ String.valueOf(id) };

        Cursor cursor = db.query(CAT_LIST_TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
        cursor.moveToFirst();

        name = cursor.getString( cursor.getColumnIndex(COL_NAME) );
        desc = cursor.getString( cursor.getColumnIndex(COL_DESC) );
        photo = cursor.getString( cursor.getColumnIndex(COL_IMG) );
        location = cursor.getString(cursor.getColumnIndex(COL_LOCATION));

        cursor.close();

        return new Cat(id, name, desc, photo, location);

    }

    public void updatePhotoByID (int id, int newValue){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_IMG, newValue);

        // Which row to update, based on the ID
        String selection = COL_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(id) };

        int count = db.update(
                CAT_LIST_TABLE_NAME,
                values,
                selection,
                selectionArgs);

        db.update(CAT_LIST_TABLE_NAME, values, selection, selectionArgs);
        db.close();
    }
    public void updateNameByID (int id, int newValue){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NAME, newValue);

        // Which row to update, based on the ID
        String selection = COL_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(id) };

        int count = db.update(
                CAT_LIST_TABLE_NAME,
                values,
                selection,
                selectionArgs);

        db.update(CAT_LIST_TABLE_NAME, values, selection, selectionArgs);
        db.close();
    }

    public void updateDescByID (int id, int newValue){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_DESC, newValue);

        // Which row to update, based on the ID
        String selection = COL_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(id) };

        int count = db.update(
                CAT_LIST_TABLE_NAME,
                values,
                selection,
                selectionArgs);

        db.update(CAT_LIST_TABLE_NAME, values, selection, selectionArgs);
        db.close();
    }

    public void updateLocationByID (int id, int newValue){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_LOCATION, newValue);

        // Which row to update, based on the ID
        String selection = COL_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(id) };

        int count = db.update(
                CAT_LIST_TABLE_NAME,
                values,
                selection,
                selectionArgs);

        db.update(CAT_LIST_TABLE_NAME, values, selection, selectionArgs);
        db.close();
    }

}