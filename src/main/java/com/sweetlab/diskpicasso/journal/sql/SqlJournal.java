package com.sweetlab.diskpicasso.journal.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import com.sweetlab.diskpicasso.CacheEntry;
import com.sweetlab.diskpicasso.journal.Journal;

import java.io.File;

/**
 * A single journal implementation using sql.
 */
public class SqlJournal extends SQLiteOpenHelper implements Journal {
    private static SqlJournal sInstance;

    private static final String EQ = " = ";
    private static final String NULL_SELECTION = null;
    private static final String[] NULL_ARGS = null;
    private static final String NULL_GROUP_BY = null;
    private static final String NULL_HAVING = null;

    private static final String SORT_OLDEST_FIRST = SqlJournalContract.EntryTable
            .COLUMN_NAME_ENTRY_TIME + " ASC";

    /**
     * All entry columns.
     */
    private static String[] ENTRY_COLUMNS = new String[]{SqlJournalContract.EntryTable
            .COLUMN_NAME_FILE_ABS_PATH, SqlJournalContract.EntryTable
            .COLUMN_NAME_CACHE_FILE_ABS_PATH, SqlJournalContract.EntryTable
            .COLUMN_NAME_BITMAP_WIDTH, SqlJournalContract.EntryTable.COLUMN_NAME_BITMAP_HEIGHT,
            SqlJournalContract.EntryTable.COLUMN_NAME_BITMAP_CONFIG,
            SqlJournalContract.EntryTable.COLUMN_NAME_BITMAP_SIZE};

    /**
     * Identity column only.
     */
    private static String[] IDENTITY_COLUMN = new String[]{SqlJournalContract.EntryTable.COLUMN_NAME_IDENTITY,};

    /**
     * Version and name.
     */
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "Pablo.db";

    /**
     * Private constructor, singleton.
     *
     * @param context Android application context.
     */
    private SqlJournal(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Get the instance.
     *
     * @param context Android application context preferably.
     * @return The journal.
     */
    public synchronized static SqlJournal getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SqlJournal(context);
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SqlJournalContract.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SqlJournalContract.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void insert(CacheEntry entry) {
        if (!exists(entry.getPrimaryKey())) {
            SQLiteDatabase db = getWritableDatabase();
            db.beginTransaction();

            try {
                ContentValues values = new ContentValues();
                values.put(SqlJournalContract.EntryTable.COLUMN_NAME_IDENTITY, entry.getPrimaryKey());
                values.put(SqlJournalContract.EntryTable.COLUMN_NAME_FILE_ABS_PATH,
                        entry.getFileKey());
                values.put(SqlJournalContract.EntryTable.COLUMN_NAME_CACHE_FILE_ABS_PATH,
                        entry.getFile().getAbsolutePath());
                values.put(SqlJournalContract.EntryTable.COLUMN_NAME_BITMAP_WIDTH, entry.getWidth());
                values.put(SqlJournalContract.EntryTable.COLUMN_NAME_BITMAP_HEIGHT,
                        entry.getHeight());
                values.put(SqlJournalContract.EntryTable.COLUMN_NAME_BITMAP_SIZE,
                        entry.getByteSize());
                values.put(SqlJournalContract.EntryTable.COLUMN_NAME_BITMAP_CONFIG,
                        entry.getConfig().name());
                values.put(SqlJournalContract.EntryTable.COLUMN_NAME_ENTRY_TIME,
                        System.currentTimeMillis());
                db.insert(SqlJournalContract.EntryTable.TABLE_NAME, null, values);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                db.close();
            }
        }
    }

    @Override
    public void remove(CacheEntry entry) {
        SQLiteDatabase db = getWritableDatabase();
        String where = SqlJournalContract.EntryTable.COLUMN_NAME_IDENTITY + EQ + entry
                .getPrimaryKey();
        db.beginTransaction();
        try {
            db.delete(SqlJournalContract.EntryTable.TABLE_NAME, where, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    @Override
    public CacheEntry[] retrieveAll() {
        CacheEntry[] entries = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            db.beginTransaction();
            cursor = db
                    .query(SqlJournalContract.EntryTable.TABLE_NAME, ENTRY_COLUMNS, NULL_SELECTION,
                            NULL_ARGS, NULL_GROUP_BY, NULL_HAVING, SORT_OLDEST_FIRST);
            if (cursor != null && cursor.moveToFirst()) {
                int count = cursor.getCount();
                entries = new CacheEntry[count];
                for (int i = 0; i < count; i++) {
                    String sourceFilePath = cursor.getString(cursor.getColumnIndex(
                            SqlJournalContract.EntryTable.COLUMN_NAME_FILE_ABS_PATH));
                    File cacheFile = new File(cursor.getString(cursor.getColumnIndex(
                            SqlJournalContract.EntryTable.COLUMN_NAME_CACHE_FILE_ABS_PATH)));
                    int width = cursor.getInt(cursor
                            .getColumnIndex(SqlJournalContract.EntryTable.COLUMN_NAME_BITMAP_WIDTH));
                    int height = cursor.getInt(cursor
                            .getColumnIndex(SqlJournalContract.EntryTable.COLUMN_NAME_BITMAP_HEIGHT));
                    Bitmap.Config config = createConfig(cursor.getString(cursor.getColumnIndex(
                            SqlJournalContract.EntryTable.COLUMN_NAME_BITMAP_CONFIG)));
                    int byteSize = cursor.getInt(cursor
                            .getColumnIndex(SqlJournalContract.EntryTable.COLUMN_NAME_BITMAP_SIZE));
                    entries[i] = new CacheEntry(sourceFilePath, cacheFile, width, height, config,
                            byteSize);
                    cursor.moveToNext();
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
            if (cursor != null) {
                cursor.close();
            }
        }
        return entries;
    }

    @Override
    public boolean exists(long identity) {
        boolean exists = false;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            db.beginTransaction();
            String IDENTITY_SELECTION = SqlJournalContract.EntryTable.COLUMN_NAME_IDENTITY + EQ + identity;

            cursor = db
                    .query(SqlJournalContract.EntryTable.TABLE_NAME, IDENTITY_COLUMN, IDENTITY_SELECTION,
                            NULL_ARGS, NULL_GROUP_BY, NULL_HAVING, SORT_OLDEST_FIRST);
            if (cursor != null && cursor.moveToFirst()) {
                int count = cursor.getCount();
                if (count > 1) {
                    throw new RuntimeException("multiple identities found for " + identity);
                }
                long dbIdentity = cursor
                        .getLong(cursor.getColumnIndex(SqlJournalContract.EntryTable.COLUMN_NAME_IDENTITY));

                exists = dbIdentity == identity;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
            if (cursor != null) {
                cursor.close();
            }
        }
        return exists;
    }

    private Bitmap.Config createConfig(String textConfig) {
        Bitmap.Config[] values = Bitmap.Config.values();
        for (Bitmap.Config config : values) {
            if (config.name().equals(textConfig)) {
                return config;
            }
        }
        throw new RuntimeException("Unknown bitmap config " + textConfig);
    }
}
