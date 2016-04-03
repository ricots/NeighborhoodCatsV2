package com.roberterrera.neighborhoodcats.sqldatabase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;


/**
 * Created by Rob on 4/3/16.
 */
public class CatsSQLiteAdapter {
    Context mContext;
    SQLiteDatabase db;
    CatsSQLiteOpenHelper helper;

    public CatsSQLiteAdapter(Context mContext) {
        this.mContext = mContext;
        helper = new CatsSQLiteOpenHelper(mContext);

    }

    // Open database
    public CatsSQLiteAdapter openDB(){
        try {
            db = helper.getWritableDatabase();
        } catch (android.database.SQLException e){
            e.printStackTrace();
        }
        return this;
    }


    // Close database
    public void closeDB(){
        try {
            helper.close();
        } catch (android.database.SQLException e){
            e.printStackTrace();
        }
    }
}
