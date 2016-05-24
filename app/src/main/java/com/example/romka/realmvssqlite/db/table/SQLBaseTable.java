package com.example.romka.realmvssqlite.db.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.romka.realmvssqlite.db.util.DbUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created on 24.05.2016.
 */
public abstract class SQLBaseTable<T> {

    public static final String FIELD_ID = "_id";
    public static final String[] COUNT = new String[]{"count(*) AS count"};
    private static final String SELECT_BY_ID = FIELD_ID + " = ?";

    protected SQLiteDatabase mSQLiteDatabase;

    public void onOpenDb(SQLiteDatabase db) {
        this.mSQLiteDatabase = db;
    }

    protected abstract String getTableName();

    /**
     * This is a method to create new item and load data into its field from database.
     *
     * @param cur opened cursor.
     * @return item instance.
     */
    protected abstract T loadDbItem(final Cursor cur);

    public abstract ContentValues convertToCV(final T item);

    public ContentValues[] convertToCV(@NonNull final List<T> items) {
        ContentValues[] list = new ContentValues[items.size()];
        int count = items.size();
        for (int i = 0; i < count; i++) {
            list[i] = convertToCV(items.get(i));
        }
        return list;
    }

    public List<T> getList(final Cursor cur, final boolean shouldCloseCursor) {
        List<T> itemList = new ArrayList<>();
        if (DbUtils.checkCursor(cur)) {
            do {
                final T item = loadDbItem(cur);
                itemList.add(item);
            }
            while (cur.moveToNext());
            if (shouldCloseCursor) cur.close();
        }
        return itemList;
    }

    public int delete(final String selection, final String[] selectionArgs) {
        return mSQLiteDatabase.delete(getTableName(), selection, selectionArgs);
    }

    public int delete(final long _id) {
        return delete(SELECT_BY_ID, getArgsForSelectById(_id));
    }

    public int deleteAll() {
        return mSQLiteDatabase.delete(
                getTableName(),
                null,
                null);
    }

    public long insertIgnoreConflict(final ContentValues _cv, final String _idColumnName) {
//        return mSQLiteDatabase.insertWithOnConflict(getTableName(), null, _cv, SQLiteDatabase.CONFLICT_IGNORE); // Not work correctly on android
        try {
            return mSQLiteDatabase.insertOrThrow(getTableName(), null, _cv);
        } catch (SQLException e) {
            e.printStackTrace();

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ");
            sql.append(_idColumnName);
            sql.append(" FROM ");
            sql.append(getTableName());
            sql.append(" WHERE ");

            Object[] bindArgs = new Object[_cv.size()];
            int i = 0;
            for (Map.Entry<String, Object> entry : _cv.valueSet()) {
                sql.append((i > 0) ? " AND " : "");
                sql.append(entry.getKey());
                sql.append(" = ?");
                bindArgs[i++] = entry.getValue();
            }

            SQLiteStatement stmt = mSQLiteDatabase.compileStatement(sql.toString());
            for (i = 0; i < bindArgs.length; i++) {
                DatabaseUtils.bindObjectToProgram(stmt, i + 1, bindArgs[i]);
            }

            try {
                return stmt.simpleQueryForLong();
            } catch (SQLiteDoneException ex) {
                ex.printStackTrace();
            } finally {
                stmt.close();
            }
            return -1;
        }
    }

    public long updateOrInsert(@Nullable ContentValues _cv) {
        if (_cv == null) return -1;
        final Long id = _cv.getAsLong(FIELD_ID);
        if (id != null) {
            update(id, _cv);
        }
        return insertIgnoreConflict(_cv, FIELD_ID);
    }

    public int update(final long oldId, final ContentValues _cv) {
        return update(_cv, SELECT_BY_ID, getArgsForSelectById(oldId));
    }

    public int update(final ContentValues _cv, final String selection, final String[] selectionArgs) {
        try {
            return mSQLiteDatabase.update(getTableName(), _cv, selection, selectionArgs);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void closeDb() {
        if (mSQLiteDatabase.isOpen()) {
            mSQLiteDatabase.close();
        }
    }

    public Cursor getAll(String[] _projection, String _selection, String[] _selectionArgs, String _orderBy, String _limit) {
        return mSQLiteDatabase.query(
                getTableName(),                    // The table name
                _projection,
                _selection,
                _selectionArgs,
                null,                               // GROUP BY clause
                null,
                _orderBy,
                _limit);
    }

    private String[] getArgsForSelectById(long _id) {
        return new String[]{String.valueOf(_id)};
    }

    public int bulkInsertOrUpdate(ContentValues[] _values) {
        mSQLiteDatabase.beginTransaction();
        int returnCount = 0;
        try {
            for (ContentValues cv : _values) {

                long rowId = updateOrInsert(cv);
                if (rowId > 0) returnCount++;
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (SQLException ex) {
            Log.w("db.bulkInsert(): ", ex);
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        return returnCount;
    }

    public boolean isOpen(boolean _withWritableAccess) {
        return mSQLiteDatabase != null && mSQLiteDatabase.isOpen() && (!_withWritableAccess || !mSQLiteDatabase.isReadOnly());
    }
}