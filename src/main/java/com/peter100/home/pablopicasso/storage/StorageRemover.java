package com.peter100.home.pablopicasso.storage;


import com.peter100.home.pablopicasso.JournalEntry;
import com.peter100.home.pablopicasso.filesystem.FileSystem;
import com.peter100.home.pablopicasso.journal.Journal;

/**
 * Remove file and journal entry from storage.
 */
public class StorageRemover {
    private final Journal mJournal;
    private final FileSystem mFileSystem;

    /**
     * Constructor.
     *
     * @param journal    Journal to work against.
     * @param fileSystem Filesystem to remove from.
     */
    public StorageRemover(Journal journal, FileSystem fileSystem) {
        mJournal = journal;
        mFileSystem = fileSystem;
    }

    /**
     * Remove a entry from cache.
     *
     * @param entry
     */
    public void remove(JournalEntry entry) {
        mFileSystem.remove(entry);
        mJournal.remove(entry);
    }
}
