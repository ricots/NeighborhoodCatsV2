package com.roberterrera.neighborhoodcats.Database;

import android.content.Context;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by Rob on 3/21/16.
 */
public class DBAssetHelper extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "CATS_DB";
    private static final int DATABASE_VERSION = 1;

    public DBAssetHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}