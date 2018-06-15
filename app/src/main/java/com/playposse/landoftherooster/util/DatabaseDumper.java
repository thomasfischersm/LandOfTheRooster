package com.playposse.landoftherooster.util;

import android.arch.persistence.db.SimpleSQLiteQuery;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility that dumps the SQLite tables to the log.
 */
public final class DatabaseDumper {

    private static final String LOG_TAG = DatabaseDumper.class.getSimpleName();

    private static final String LIST_TABLES_SQL =
            "select name from sqlite_master where type='table'";

    public static void dumpTables(SupportSQLiteOpenHelper openHelper) {
        SupportSQLiteDatabase readableDatabase = openHelper.getReadableDatabase();

        try {
            List<String> tableNames = queryTableNames(readableDatabase);
            for (String tableName : tableNames) {
                dumpTable(tableName, readableDatabase);
            }
        } finally {
//            readableDatabase.close();
        }
    }

    private static List<String> queryTableNames(SupportSQLiteDatabase readableDatabase) {
        Cursor cursor = readableDatabase.query(LIST_TABLES_SQL, null);
        List<String> tableNames = new ArrayList<>();

        try {
            while (cursor.moveToNext()) {
                String tableName = cursor.getString(0);

                tableNames.add(tableName);
            }
        } finally {
            cursor.close();
        }

        return tableNames;
    }

    private static void dumpTable(String tableName, SupportSQLiteDatabase readableDatabase) {
        // Query db.
        Cursor cursor = readableDatabase.query(new SimpleSQLiteQuery("select * from " + tableName));
        String[] columnNames = cursor.getColumnNames();

        // Read data.
        List<List<String>> result = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                List<String> row = new ArrayList<>();
                result.add(row);
                for (String columnName : columnNames) {
                    row.add(cursor.getString(cursor.getColumnIndex(columnName)));
                }
            }
        } finally {
            cursor.close();
        }

        // Write table name.
        Log.d(LOG_TAG, "dumpTable: === " + tableName + " (" + result.size() + " rows ) ===");

        // Calculate column widths.
        List<Integer> columnWidths = new ArrayList<>();
        for (int i = 0; i < columnNames.length; i++) {
            String columnName = columnNames[i];
            columnWidths.add(getMaxColumnWidth(result, i, columnName));
        }

        // Format header.
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columnNames.length; i++) {
            String columnName = columnNames[i];
            sb.append("|");
            sb.append(columnName.toUpperCase());
            int spaceLength = columnWidths.get(i) - columnName.length();
            sb.append(new String(new char[spaceLength]).replace('\0', ' '));
        }
        Log.d(LOG_TAG, "dumpTable: " + sb.toString());

        // Format data.
        for (int i = 0; i < result.size(); i++) {
            List<String> row = result.get(i);
            StringBuilder rowBuilder = new StringBuilder();
            for (int j = 0; j < columnNames.length; j++) {
                rowBuilder.append("|");
                String value = row.get(j);

                if (value == null) {
                    value = "";
                }

                rowBuilder.append(value);
                int spaceLength = columnWidths.get(j) - value.length();
                rowBuilder.append(new String(new char[spaceLength]).replace('\0', ' '));
            }
            Log.d(LOG_TAG, "dumpTable: " + rowBuilder);
        }

        Log.d(LOG_TAG, "dumpTable: \n\n\n");
    }

    private static int getMaxColumnWidth(
            List<List<String>> result,
            int columnIndex,
            String columnName) {
        int max = 0;
        for (List<String> row : result) {
            String value = row.get(columnIndex);
            if (value != null) {
                max = Math.max(max, value.length());
            }
        }
        return Math.max(max, columnName.length());
    }
}

