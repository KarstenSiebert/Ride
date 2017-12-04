package com.nedeos.ride.messages;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nedeos.ride.RideApplication;

import static android.provider.BaseColumns._ID;

/**
 * Created by Karsten on 20.09.2017.
 */

public class RideDBHelper extends SQLiteOpenHelper {

    private static final String SQL_CREATE_MESSAGES = "CREATE TABLE IF NOT EXISTS " + RideApplication.DB_MESSAGES_TABLE + " ( " + _ID + " INTEGER PRIMARY KEY," + " head TEXT, text TEXT, link TEXT, icon TEXT, shot TEXT, prod TEXT, time INTEGER, noid INTEGER )";
    private static final String SQL_DELETE_MESSAGES = "DROP TABLE IF EXISTS " + RideApplication.DB_MESSAGES_TABLE;

    private static final String SQL_CREATE_SUBSCRIPTIONS = "CREATE TABLE IF NOT EXISTS " + RideApplication.DB_SUBSCRIPTIONS_TABLE + " ( " + _ID + " INTEGER PRIMARY KEY," + " prod TEXT, head TEXT, text TEXT, icon TEXT, shot TEXT, cost TEXT, used TEXT, stat INTEGER, time INTEGER, usid INTEGER )";
    private static final String SQL_DELETE_SUBSCRIPTIONS = "DROP TABLE IF EXISTS " + RideApplication.DB_SUBSCRIPTIONS_TABLE;

    private static final String SQL_CREATE_FOUNDSUBS = "CREATE TABLE IF NOT EXISTS " + RideApplication.DB_FOUNDSUBS_TABLE + " ( " + _ID + " INTEGER PRIMARY KEY," + " prod TEXT, head TEXT, text TEXT, icon TEXT, shot TEXT, cost TEXT, used TEXT, stat INTEGER, time INTEGER, usid INTEGER )";
    private static final String SQL_DELETE_FOUNDSUBS = "DROP TABLE IF EXISTS " + RideApplication.DB_FOUNDSUBS_TABLE;

    private static final int DATABASE_VERSION = 10;
    private static final String DATABASE_NAME = "RideDB.db";

    public RideDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_MESSAGES);
        sqLiteDatabase.execSQL(SQL_CREATE_FOUNDSUBS);
        sqLiteDatabase.execSQL(SQL_CREATE_SUBSCRIPTIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_MESSAGES);
        sqLiteDatabase.execSQL(SQL_DELETE_FOUNDSUBS);
        sqLiteDatabase.execSQL(SQL_DELETE_SUBSCRIPTIONS);

        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        onUpgrade(sqLiteDatabase, oldVersion, newVersion);
    }

    public static void delAllFromDB(final String table) {
        final RideDBHelper rideDBHelper = RideApplication.getRideDBHelper();

        if (rideDBHelper != null) {
            final SQLiteDatabase db = rideDBHelper.getWritableDatabase();

            if (db != null) {
                db.delete(table, null, null);
            }
        }
    }

    public static void addSubToDB(Subscription subscription, final String table) {
        final RideDBHelper rideDBHelper = RideApplication.getRideDBHelper();

        if (rideDBHelper != null) {
            final SQLiteDatabase db = rideDBHelper.getWritableDatabase();

            if (db != null) {
                ContentValues values = new ContentValues();

                values.put(RideApplication.ARG_PROD, subscription.getProd());
                values.put(RideApplication.ARG_HEAD, subscription.getHead());
                values.put(RideApplication.ARG_TEXT, subscription.getText());
                values.put(RideApplication.ARG_ICON, subscription.getIcon());
                values.put(RideApplication.ARG_SHOT, subscription.getShot());
                values.put(RideApplication.ARG_COST, subscription.getCost());
                values.put(RideApplication.ARG_USED, subscription.getUsed());
                values.put(RideApplication.ARG_TIME, subscription.getTime());
                values.put(RideApplication.ARG_STAT, subscription.getStat());
                values.put(RideApplication.ARG_USID, subscription.getUsid());

                db.insert(table, null, values);
            }
        }
    }

    public static int getCountFromDB(final String table) {
        int counter = 0;

        final RideDBHelper rideDBHelper = RideApplication.getRideDBHelper();

        if (rideDBHelper != null) {
            final SQLiteDatabase db = rideDBHelper.getReadableDatabase();

            if (db != null) {
                Cursor mCount = db.rawQuery("SELECT COUNT(*) FROM " + table, null);

                mCount.moveToFirst();

                counter = mCount.getInt(0);

                mCount.close();
            }
        }

        return counter;
    }

    public static void setUsedInSubDB(final String table, Subscription subscription, final String used) {
        final RideDBHelper rideDBHelper = RideApplication.getRideDBHelper();

        if (rideDBHelper != null) {
            final SQLiteDatabase db = rideDBHelper.getWritableDatabase();

            if (db != null) {
                subscription.setUsed(used);

                ContentValues values = new ContentValues();

                values.put(RideApplication.ARG_USED, used);

                db.update(table, values, "prod = ?", new String[]{subscription.getProd()});
            }
        }
    }

}
