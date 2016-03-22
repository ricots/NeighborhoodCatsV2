package com.roberterrera.neighborhoodcats;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Rob on 3/21/16.
 */
public class CatsSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = CatsSQLiteOpenHelper.class.getCanonicalName();

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "CATS_DB";
    public static final String CAT_LIST_TABLE_NAME = "YOUR_CATS";

    public static final String COL_ID = "_id";
    public static final String COL_ITEM_NAME = "CAT_NAME";
    public static final String COL_DESC = "DESCRIPTION";
    public static final String COL_LOCATION = "LOCATION";
    public static final String COL_IMG = "IMAGE_PATH";
    public static final String[] CATS_COLUMNS = {COL_ID,COL_ITEM_NAME,COL_DESC, COL_LOCATION, COL_IMG};

    public CatsSQLiteOpenHelper (Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    private static final String CREATE_CAT_LIST_TABLE =
            "CREATE TABLE " + CAT_LIST_TABLE_NAME +
                    "(" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_ITEM_NAME + " TEXT, " +
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

        return cursor;
    }


    public void deleteCatByID(int id){

        SQLiteDatabase db = getWritableDatabase();

        String selection = "_id = ?";
        String[] selectionArgs = { String.valueOf(COL_ID) };

        db.delete(CAT_LIST_TABLE_NAME,
                selection,
                selectionArgs);
    }

    // TODO: Add methods to add and edit cat entries.


}