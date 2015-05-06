package com.peter100.home.pablopicasso.storage;

import android.graphics.Bitmap;

import com.peter100.home.pablopicasso.JournalEntry;
import com.peter100.home.pablopicasso.filesystem.FileSystem;
import com.peter100.home.pablopicasso.journal.Journal;

/**
 * Write file and journal entry from storage.
 */
public class StorageWriter {
    private final Journal mJournal;
    private final FileSystem mFileSystem;

    /**
     * Constructor.
     *
     * @param journal    Journal to use for entry tracking.
     * @param fileSystem Filesystem to use for image cache files.
     */
    public StorageWriter(Journal journal, FileSystem fileSystem) {
        mJournal = journal;
        mFileSystem = fileSystem;
    }

    /**
     * Make a write.
     *
     * @param filePath Original image path.
     * @param bitmap   Bitmap to store.
     * @return Entry or null if fail.
     */
    public JournalEntry write(String filePath, Bitmap bitmap) {
        JournalEntry entry = mFileSystem.write(filePath, bitmap);
        if (entry != null) {
            mJournal.insert(entry);
            return entry;
        }
        return null;
    }
}
