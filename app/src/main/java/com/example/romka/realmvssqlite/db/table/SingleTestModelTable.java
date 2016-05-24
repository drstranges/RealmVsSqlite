package com.example.romka.realmvssqlite.db.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.romka.realmvssqlite.model.SingleTestModel;

/**
 * Created on 24.05.2016.
 */
public class SingleTestModelTable extends SQLBaseTable<SingleTestModel> {

    public static final String TABLE_NAME = "SingleTestModelTable";

    public static final String FIELD_LABEL = "label";
    public static final String FIELD_LONG_VALUE = "longValue";

    protected static final String SCRIPT_CREATE_TABLE = "create table " + TABLE_NAME + " ( " +
            FIELD_ID + " integer primary key, " +
            FIELD_LABEL + " text not null, " +
            FIELD_LONG_VALUE + " integer" +
            ");";

    public static void onCreateDb(SQLiteDatabase db) throws SQLException {
        db.execSQL(SCRIPT_CREATE_TABLE);
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected SingleTestModel loadDbItem(final Cursor _cur) {
        final SingleTestModel item = new SingleTestModel();

        item.id = _cur.getInt(_cur.getColumnIndex(FIELD_ID));
        item.label = _cur.getString(_cur.getColumnIndex(FIELD_LABEL));
        item.longValue = _cur.getLong(_cur.getColumnIndex(FIELD_LONG_VALUE));

        return item;
    }

    @Override
    public ContentValues convertToCV(final SingleTestModel item) {
        ContentValues cv = new ContentValues();
        final Integer id = item.id;
        if (id != null) {
            cv.put(FIELD_ID, id);
        }
        cv.put(FIELD_LABEL, item.label);
        cv.put(FIELD_LONG_VALUE, item.longValue);

        return cv;
    }

}
