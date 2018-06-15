package com.playposse.landoftherooster.util;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.playposse.landoftherooster.contentprovider.room.RoosterDatabase;

/**
 * A utility for dealing with Sqlite.
 */
public final class SqliteUtil {

    private static final String LOG_TAG = SqliteUtil.class.getSimpleName();

    private SqliteUtil() {}

    public static void explain(Context context, String sql) {
        final RoosterDatabase database = RoosterDatabase.getInstance(context);

        Cursor cursor = database.query(sql, null);
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                if (sb.length() > 0) {
                    sb.append("| ");
                }
                sb.append(cursor.getColumnName(i));
            }
            Log.d(LOG_TAG, "explain: " + sb.toString());

            while (cursor.moveToNext()) {
                sb = new StringBuilder();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    if (sb.length() > 0) {
                        sb.append("| ");
                    }
                    sb.append(cursor.getString(i));
                }
                Log.d(LOG_TAG, "explain: " + sb.toString());
            }
        } finally {
            cursor.close();
        }
    }
}
