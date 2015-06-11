package com.swproject.fi.workshopa;

/**
 * Created by alex on 11.6.2015.
 */

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;

import com.aware.Aware;
import com.aware.utils.DatabaseHelper;

import java.util.HashMap;

public class Provider extends ContentProvider {

    public static final int DATABASE_VERSION = 1;

    public static String AUTHORITY = "com.swproject.fi.workshopa.provider.catchy_data";
    public static final String DATABASE_NAME = Environment.getExternalStorageDirectory() + "/AWARE/catchy_data.db";

    private static final int PLUGIN_INDOOR = 2;
    private static final int PLUGIN_INDOOR_ID = 6;

    private static UriMatcher URIMatcher;
    private static HashMap<String, String> databaseMap;
    private static DatabaseHelper databaseHelper;
    private static SQLiteDatabase database;

    public static final String[] DATABASE_TABLES = {
            "catchy_data"
    };

    public static final String[] TABLES_FIELDS = {
            Plugin_Data._ID + " integer primary key autoincrement," +
                    Plugin_Data.TIMESTAMP + " real default 0," +
                    Plugin_Data.DEVICE_ID + " text default ''," +
                    //Plugin_Data.MAGNETOMETER + "real default 0" +
                    Plugin_Data.UPDATE_FREQUENCY + " real default 0," +
                    Plugin_Data.ACCEL_AXIS_X + " real default 0," +
                    Plugin_Data.ACCEL_AXIS_Y + " real default 0," +
                    Plugin_Data.ACCEL_AXIS_Z + " real default 0," +
                    Plugin_Data.GYRO_AXIS_X + " real default 0," +
                    Plugin_Data.GYRO_AXIS_Y + " real default 0," +
                    Plugin_Data.GYRO_AXIS_Z + " real default 0," +
                    Plugin_Data.MAGNET_AXIS_X + " real default 0," +
                    Plugin_Data.MAGNET_AXIS_Y + " real default 0," +
                    Plugin_Data.MAGNET_AXIS_Z + " real default 0," +
                    Plugin_Data.LABEL + " integer default 0," +
                    Plugin_Data.COUNT + " integer default 0," +
                    Plugin_Data.ELAPSED_OUTDOOR + " real default 0," +
                    Plugin_Data.ELAPSED_INDOOR + " real default 0," +
                    "UNIQUE("+Plugin_Data.TIMESTAMP+","+Plugin_Data.DEVICE_ID+")"
    };

    public static final class Plugin_Data implements BaseColumns {
        private Plugin_Data(){}

        public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/catchy_data");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.catchy.data";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.catchy.data";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String UPDATE_FREQUENCY = "double_frequency";
        public static final String ACCEL_AXIS_X = "accel_axis_x";
        public static final String ACCEL_AXIS_Y = "accel_axis_y";
        public static final String ACCEL_AXIS_Z = "accel_axis_z";
        public static final String GYRO_AXIS_X = "gyro_axis_x";
        public static final String GYRO_AXIS_Y = "gyro_axis_y";
        public static final String GYRO_AXIS_Z = "gyro_axis_z";
        public static final String MAGNET_AXIS_X = "magnet_axis_x";
        public static final String MAGNET_AXIS_Y = "magnet_axis_y";
        public static final String MAGNET_AXIS_Z = "magnet_axis_z";
        public static final String LABEL = "label";
        public static final String COUNT = "count";


        public static final String ELAPSED_OUTDOOR = "elapsed_outdoor";
        public static final String ELAPSED_INDOOR = "elapsed_indoor";
    }

    @Override
    public boolean onCreate() {
        AUTHORITY = getContext().getPackageName() + ".provider.catchy_data";
        URIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        URIMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], PLUGIN_INDOOR);
        URIMatcher.addURI(AUTHORITY, DATABASE_TABLES[0]+"/#", PLUGIN_INDOOR_ID);

        databaseMap = new HashMap<>();
        databaseMap.put(Plugin_Data._ID, Plugin_Data._ID);
        databaseMap.put(Plugin_Data.TIMESTAMP, Plugin_Data.TIMESTAMP);
        databaseMap.put(Plugin_Data.DEVICE_ID, Plugin_Data.DEVICE_ID);
        databaseMap.put(Plugin_Data.UPDATE_FREQUENCY, Plugin_Data.UPDATE_FREQUENCY);
        databaseMap.put(Plugin_Data.ACCEL_AXIS_X, Plugin_Data.ACCEL_AXIS_X);
        databaseMap.put(Plugin_Data.ACCEL_AXIS_Y, Plugin_Data.ACCEL_AXIS_Y);
        databaseMap.put(Plugin_Data.ACCEL_AXIS_Z, Plugin_Data.ACCEL_AXIS_Z);
        databaseMap.put(Plugin_Data.GYRO_AXIS_X, Plugin_Data.GYRO_AXIS_X);
        databaseMap.put(Plugin_Data.GYRO_AXIS_Y, Plugin_Data.GYRO_AXIS_Y);
        databaseMap.put(Plugin_Data.GYRO_AXIS_Z, Plugin_Data.GYRO_AXIS_Z);
        databaseMap.put(Plugin_Data.MAGNET_AXIS_X, Plugin_Data.MAGNET_AXIS_X);
        databaseMap.put(Plugin_Data.MAGNET_AXIS_Y, Plugin_Data.MAGNET_AXIS_Y);
        databaseMap.put(Plugin_Data.MAGNET_AXIS_Z, Plugin_Data.MAGNET_AXIS_Z);
        databaseMap.put(Plugin_Data.LABEL, Plugin_Data.LABEL);
        databaseMap.put(Plugin_Data.COUNT, Plugin_Data.COUNT);
        databaseMap.put(Plugin_Data.ELAPSED_OUTDOOR, Plugin_Data.ELAPSED_OUTDOOR);
        databaseMap.put(Plugin_Data.ELAPSED_INDOOR, Plugin_Data.ELAPSED_INDOOR);

        return true;
    }

    private boolean initializeDB() {
        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS);
        }
        if( databaseHelper != null && ( database == null || ! database.isOpen() )) {
            database = databaseHelper.getWritableDatabase();
        }
        return( database != null && databaseHelper != null);
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings2, String s2) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY, "Database unavailable...");
            return null;
        }

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (URIMatcher.match(uri)) {
            case PLUGIN_INDOOR:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(databaseMap);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            Cursor c = qb.query(database, strings, s, strings2,
                    null, null, s2);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        } catch (IllegalStateException e) {
            if (Aware.DEBUG)
                Log.e(Aware.TAG, e.getMessage());

            return null;
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (URIMatcher.match(uri)) {
            case PLUGIN_INDOOR:
                return Plugin_Data.CONTENT_TYPE;
            case PLUGIN_INDOOR_ID:
                return Plugin_Data.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return null;
        }

        ContentValues values = (contentValues != null) ? new ContentValues(
                contentValues) : new ContentValues();

        switch (URIMatcher.match(uri)) {
            case PLUGIN_INDOOR:
                long weather_id = database.insert(DATABASE_TABLES[0], Plugin_Data.DEVICE_ID, values);

                if (weather_id > 0) {
                    Uri new_uri = ContentUris.withAppendedId(Plugin_Data.CONTENT_URI, weather_id);
                    getContext().getContentResolver().notifyChange(new_uri,
                            null);
                    return new_uri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY, "Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (URIMatcher.match(uri)) {
            case PLUGIN_INDOOR:
                count = database.delete(DATABASE_TABLES[0], s, strings);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (URIMatcher.match(uri)) {
            case PLUGIN_INDOOR:
                count = database.update(DATABASE_TABLES[0], contentValues, s,
                        strings);
                break;
            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}

