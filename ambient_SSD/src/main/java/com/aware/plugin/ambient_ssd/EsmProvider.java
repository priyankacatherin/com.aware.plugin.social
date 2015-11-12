package com.aware.plugin.ambient_ssd;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.aware.utils.DatabaseHelper;

import java.util.HashMap;

/**
 * Created by admin on 8/30/2015.
 */
public class EsmProvider extends ContentProvider {
    public static String AUTHORITY = "com.aware.provider.esm.example";
    public static final int DATABASE_VERSION = 4;
    private static final int ESM = 1;
    private static final int ESM_ID = 2;


    public static final class Esm_Data implements BaseColumns {
        private Esm_Data() {

        };


        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/esm_example");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.esm.example";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.esm.example";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String LOCATION = "Location";
        public static final String SITUATION= "situation";
      //  public static final String EMAIL="email";

    }

  //  public static String DATABASE_NAME = Environment
    //        .getExternalStorageDirectory() + "/AWARE/" + "esm_example.db";
    public static String DATABASE_NAME="esm_example.db";
    public static final String[] DATABASE_TABLES = {"esm_example"};

    public static final String[] TABLES_FIELDS = {
            // screen
            Esm_Data._ID + " integer primary key autoincrement,"
                    + Esm_Data.TIMESTAMP + " real default 0,"
                    + Esm_Data.DEVICE_ID + " text default '',"
                    + Esm_Data.LOCATION + " text default '',"
                    +Esm_Data.SITUATION+ " text default ''," + "UNIQUE("
                    + Esm_Data.TIMESTAMP + "," + Esm_Data.DEVICE_ID + ")"};

    private static UriMatcher sUriMatcher = null;
    private static HashMap<String, String> tableMap = null;
    private static DatabaseHelper databaseHelper = null;
    private static SQLiteDatabase database = null;

    private boolean initializeDB() {
        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS);
        }
        if (databaseHelper != null && (database == null || !database.isOpen())) {
            database = databaseHelper.getWritableDatabase();
        }
        return (database != null && databaseHelper != null);
    }

    @Override

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (!initializeDB()) {
            Log.w(AUTHORITY, "Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case ESM:
                database.beginTransaction();
                count = database.delete(DATABASE_TABLES[0], selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }


    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case ESM:
                return Esm_Data.CONTENT_TYPE;
            case ESM_ID:
                return Esm_Data.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (!initializeDB()) {
            Log.w(AUTHORITY, "Database unavailable...");
            return null;
        }

        ContentValues values = (initialValues != null) ? new ContentValues(
                initialValues) : new ContentValues();

        switch (sUriMatcher.match(uri)) {
            case ESM:
                long _id = database.insert(DATABASE_TABLES[0], Esm_Data.DEVICE_ID, values);
                if (_id > 0) {
                    Uri dataUri = ContentUris.withAppendedId(Esm_Data.CONTENT_URI, _id);
                    getContext().getContentResolver().notifyChange(dataUri, null);
                    return dataUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);

        }
    }

    @Override
    public boolean onCreate() {
        AUTHORITY = getContext().getPackageName() + ".provider.esm.example";

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(EsmProvider.AUTHORITY, DATABASE_TABLES[0],
                ESM);
        sUriMatcher.addURI(EsmProvider.AUTHORITY,
                DATABASE_TABLES[0] + "/#", ESM_ID);

        tableMap = new HashMap<String, String>();
        tableMap.put(Esm_Data._ID, Esm_Data._ID);
        tableMap.put(Esm_Data.TIMESTAMP, Esm_Data.TIMESTAMP);
        tableMap.put(Esm_Data.DEVICE_ID, Esm_Data.DEVICE_ID);
     //   tableMap.put(Esm_Data.EMAIL,Esm_Data.EMAIL);
        tableMap.put(Esm_Data.LOCATION,
                Esm_Data.LOCATION);
        tableMap.put(Esm_Data.SITUATION,
                Esm_Data.SITUATION);


        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }
}

