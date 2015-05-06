package com.peter100.home.pablopicasso.journal.sql;

/**
 * Contract against sql database.
 */
public class SqlJournalContract {
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    /**
     * The create statement.
     */
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + EntryTable.TABLE_NAME + " (" +
                    EntryTable.COLUMN_NAME_IDENTITY + " INTEGER PRIMARY KEY," +
                    EntryTable.COLUMN_NAME_FILE_ABS_PATH + TEXT_TYPE + COMMA_SEP +
                    EntryTable.COLUMN_NAME_CACHE_FILE_ABS_PATH + TEXT_TYPE + COMMA_SEP +
                    EntryTable.COLUMN_NAME_BITMAP_WIDTH + INTEGER_TYPE + COMMA_SEP +
                    EntryTable.COLUMN_NAME_BITMAP_HEIGHT + INTEGER_TYPE + COMMA_SEP +
                    EntryTable.COLUMN_NAME_BITMAP_SIZE + INTEGER_TYPE + COMMA_SEP +
                    EntryTable.COLUMN_NAME_BITMAP_CONFIG + TEXT_TYPE + COMMA_SEP +
                    EntryTable.COLUMN_NAME_ENTRY_TIME + INTEGER_TYPE + " )";

    /**
     * The delete statement.
     */
    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + EntryTable.TABLE_NAME;

    /**
     * The table containing cache entry information.
     */
    public static class EntryTable {
        public static final String TABLE_NAME = "entry_table";
        public static final String COLUMN_NAME_IDENTITY = "identity";
        public static final String COLUMN_NAME_FILE_ABS_PATH = "file_abs_path";
        public static final String COLUMN_NAME_CACHE_FILE_ABS_PATH = "cache_file_abs_path";
        public static final String COLUMN_NAME_BITMAP_WIDTH = "bitmap_width";
        public static final String COLUMN_NAME_BITMAP_HEIGHT = "bitmap_height";
        public static final String COLUMN_NAME_BITMAP_SIZE = "bitmap_size";
        public static final String COLUMN_NAME_BITMAP_CONFIG = "bitmap_config";
        public static final String COLUMN_NAME_ENTRY_TIME = "entry_time";
    }
}
