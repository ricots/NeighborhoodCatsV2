package com.roberterrera.neighborhoodcats.sqldatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.roberterrera.neighborhoodcats.models.Cat;

/**
 * Created by Rob on 3/21/16.
 */
public class CatsSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = CatsSQLiteOpenHelper.class.getCanonicalName();

    private static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "CATS_DB.db";
    public static final String CAT_LIST_TABLE_NAME = "YOUR_CATS";

    public static final String CAT_ID = "_id";
    public static final String CAT_NAME = "CAT_NAME";
    public static final String CAT_DESC = "CAT_DESC";
    public static final String CAT_LAT = "CAT_LAT";
    public static final String CAT_LONG = "CAT_LONG";
    public static final String CAT_IMG = "IMAGE_PATH";
    public static final String[] CATS_COLUMNS = {CAT_ID, CAT_NAME, CAT_DESC, CAT_LAT, CAT_LONG, CAT_IMG};

    public CatsSQLiteOpenHelper (Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    private static final String CREATE_CAT_LIST_TABLE =
            "CREATE TABLE " + CAT_LIST_TABLE_NAME +
                    "(" +
                    CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    CAT_NAME + " TEXT, " +
                    CAT_DESC + " TEXT, " +
                    CAT_LAT + " INTEGER, " +
                    CAT_LONG + " INTEGER, " +
                    CAT_IMG + " TEXT)";

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

        if (cursor != null) {
            cursor.moveToFirst();
        }
        db.close();
        return cursor;
    }

    public Cursor searchCats(String query){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(CAT_LIST_TABLE_NAME, // a. table
                CATS_COLUMNS, // b. column names
                // To search across name or desc.
                CAT_NAME + " LIKE ?" + " OR " + CAT_DESC + " LIKE ?", // c. selections
                new String[]{"%" + query + "%", "%" + query + "%"}, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit

        while (!cursor.isAfterLast()){
            cursor.moveToNext();
        }
        db.close();
        return cursor;
    }

    public void deleteCatByID(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(CAT_LIST_TABLE_NAME, CAT_ID + "=" + id, null);
        db.close();
    }

    public void insert(String name, String desc, double latitude, double longitude, String photo){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CAT_NAME, name);
        values.put(CAT_DESC, desc);
        values.put(CAT_LAT, latitude);
        values.put(CAT_LONG, longitude);
        values.put(CAT_IMG, photo);

        db.insert(CAT_LIST_TABLE_NAME, null, values);
        db.close();
    }

    public double getCatLatByID(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(CAT_LIST_TABLE_NAME,
                new String[]{CAT_LAT},
                CAT_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null,
                null );

        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor.getDouble(cursor.getColumnIndex(CAT_LAT));
    }

    public double getCatLongByID(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(CAT_LIST_TABLE_NAME,
                new String[]{CAT_LONG},
                CAT_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null,
                null );

        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor.getDouble(cursor.getColumnIndex(CAT_LONG));
    }

    public String getCatNameByID(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(CAT_LIST_TABLE_NAME,
            new String[]{CAT_NAME},
            CAT_ID + " = ?",
            new String[]{String.valueOf(id)},
            null,
            null,
            null,
            null );

        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor.getString(cursor.getColumnIndex(CAT_NAME));
    }

    public String getCatDescByID(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(CAT_LIST_TABLE_NAME,
                new String[]{CAT_DESC},
                CAT_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null,
                null );

        if (cursor != null) {
            cursor.moveToFirst();
        }

        return cursor.getString(cursor.getColumnIndex(CAT_DESC));
    }

    public String getCatPhotoByID(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(CAT_LIST_TABLE_NAME,
                new String[]{CAT_IMG},
                CAT_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null,
                null );

        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor.getString(cursor.getColumnIndex(CAT_IMG));
    }

    public Cat getCatByID(int id){
        SQLiteDatabase db = getReadableDatabase();

        String[] columns = CATS_COLUMNS;

        String selection = "id = ?";
        String[] selectionArgs = new String[]{ String.valueOf(id) };

        Cursor cursor = db.query(CAT_LIST_TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
        cursor.moveToFirst();

        String name = cursor.getString( cursor.getColumnIndex(CAT_NAME) );
        String desc = cursor.getString( cursor.getColumnIndex(CAT_DESC) );
        double latitude = cursor.getColumnIndex(CAT_LAT);
        double longitude = cursor.getColumnIndex(CAT_LONG);
        String photo = cursor.getString(cursor.getColumnIndex(CAT_IMG));

        cursor.close();

        return new Cat(id, name, desc, latitude, longitude, photo);

    }

    public void updatePhotoByID (int id, int newValue){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CAT_IMG, newValue);

        // Which row to update, based on the ID
        String selection = CAT_ID + " LIKE ?";
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
        values.put(CAT_NAME, newValue);

        // Which row to update, based on the ID
        String selection = CAT_ID + " LIKE ?";
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
        values.put(CAT_DESC, newValue);

        // Which row to update, based on the ID
        String selection = CAT_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(id) };

        int count = db.update(
                CAT_LIST_TABLE_NAME,
                values,
                selection,
                selectionArgs);

        db.update(CAT_LIST_TABLE_NAME, values, selection, selectionArgs);
        db.close();
    }

//    public void updateLocationByID (int id, int newValue){
//
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        ContentValues values = new ContentValues();
//        values.put(COL_LOCATION, newValue);
//
//        // Which row to update, based on the ID
//        String selection = CAT_ID + " LIKE ?";
//        String[] selectionArgs = { String.valueOf(id) };
//
//        int count = db.update(
//                CAT_LIST_TABLE_NAME,
//                values,
//                selection,
//                selectionArgs);
//
//        db.update(CAT_LIST_TABLE_NAME, values, selection, selectionArgs);
//        db.close();
//    }

}