package com.example.romka.realmvssqlite.db.util;

import android.database.Cursor;

/**
 * Created on 24.05.2016.
 */
public class DbUtils {
    /**
     * This method check cursor to Null or Empty.
     * If cursor is Empty then execute close.
     * If cursor not Null and not Empty then execute moveToFirst.
     *
     * @param cur opened cursor.
     * @return False if cursor is null or empty, True otherwise.
     */
    public static boolean checkCursor(Cursor cur) {
        if (cur == null) return false;
        if (!cur.moveToFirst()) {
            cur.close();
            return false;
        }
        return true;
    }

    public static int getCount(Cursor _cur) {
        int res = 0;
        if (DbUtils.checkCursor(_cur)) {
            res = _cur.getInt(0);
            _cur.close();
        }
        return res;
    }
}
