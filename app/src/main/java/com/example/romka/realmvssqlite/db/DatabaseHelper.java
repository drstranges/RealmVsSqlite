package com.example.romka.realmvssqlite.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.example.romka.realmvssqlite.db.table.SingleTestModelTable;
import com.example.romka.realmvssqlite.model.SingleTestModel;

import java.util.Collections;
import java.util.List;

/**
 * Created on 24.05.2016.
 */
public class DatabaseHelper  extends SQLiteOpenHelper {

//    public static final String LOG_TAG = LogHelper.makeLogTag(DatabaseHelper.class);

    private static final String DATABASE_NAME = "forecast.db";
    private static final int DATABASE_VERSION = 1;

    private volatile static DatabaseHelper sInstance = null;
    private static final Object sLock = new Object();

    private final SingleTestModelTable mSingleTestModelTable = new SingleTestModelTable();

    public static DatabaseHelper getInstance(Context _context) {
        if (_context == null)
            throw new RuntimeException("DatabaseHelper not initialized. You cannot get the instance until you initialized that");

        DatabaseHelper instance = sInstance;
        if (instance == null) {
            synchronized (sLock) {
                instance = sInstance;
                if (instance == null) {
                    sInstance = instance = new DatabaseHelper(_context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private DatabaseHelper(Context _context) {
        super(_context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase _db) {
//        LogHelper.LOGI(LOG_TAG, "onCreate");
        try {
            SingleTestModelTable.onCreateDb(_db);
        } catch (SQLException e) {
//            LogHelper.LOGE(LOG_TAG, "Can't create database. Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void dropTables(final SQLiteDatabase _db) {
//        LogHelper.LOGI(LOG_TAG, "Drop database");
        try {
            _db.execSQL("DROP TABLE " + SingleTestModelTable.TABLE_NAME);
        } catch (SQLException e) {
//            LogHelper.LOGE(LOG_TAG, "Can't drop database. Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(final SQLiteDatabase _db, final int _oldVersion, final int _newVersion) {
//        LogHelper.LOGI(LOG_TAG, "onUpdate from v." + _oldVersion + " to v." + _newVersion);
        switch (_oldVersion) {
//                case 1:
//                    upgradeToSecondVersion(_db, _oldVersion, _newVersion);
//                case 2:
//                    upgradeToThirdVersion(_db, _oldVersion, _newVersion);
            default:
                dropTables(_db);
                break;
        }
        onCreate(_db);
//        LogHelper.LOGI(LOG_TAG, "Database has been upgraded successfully.");
    }

    @Override
    public void onDowngrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
        dropTables(_db);
        onCreate(_db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        mSingleTestModelTable.onOpenDb(db);
    }

    public SingleTestModelTable openSingleTestModelTable(boolean _withWritableAccess) {
        openDb(_withWritableAccess);
        return mSingleTestModelTable;
    }

    public <T> T convertFromCursor(Cursor _cursor, Class<T> _class, boolean _shouldCloseCursor) {
        List<T> list = convertCursorToList(_cursor, _class, _shouldCloseCursor);
        return list.isEmpty() ? null : list.get(0);
    }

    public <T> List<T> convertCursorToList(Cursor _cursor, Class<T> _class, boolean _shouldCloseCursor) {
        try {
            if (SingleTestModel.class.equals(_class)) {
                //noinspection unchecked
                return (List<T>) mSingleTestModelTable.getList(_cursor, _shouldCloseCursor);
            }// else if
        } catch (ClassCastException e) {
//            LogHelper.LOGE(LOG_TAG, "convertCursorToList exception", e);
        }
        return Collections.emptyList();
    }

    public <T> ContentValues[] convertToCv(@NonNull List<T> items, Class<T> _class) {
        try {
            if (SingleTestModel.class.equals(_class)) {
                return mSingleTestModelTable.convertToCV((List<SingleTestModel>) items);
            }// else if
        } catch (ClassCastException e) {
//            LogHelper.LOGE(LOG_TAG, "convertToCv exception", e);
        }
        return new ContentValues[0];
    }


    private void openDb(boolean _withWritableAccess) {
        if (_withWritableAccess) {
            getWritableDatabase();
        } else {
            getReadableDatabase();
        }
    }

    // Delete all data in the database
    public void clearData() {
        dropTables(getWritableDatabase());
        onCreate(getWritableDatabase());
    }

}
