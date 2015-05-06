package com.peter100.home.pablopicasso.storage;

import android.content.Context;
import android.graphics.Bitmap;

import com.peter100.home.pablopicasso.JournalEntry;
import com.peter100.home.pablopicasso.filesystem.FileSystem;
import com.peter100.home.pablopicasso.journal.Journal;

/**
 * Storage supporting file and journal write, read and remove operation.
 */
public class Storage {
    private static final Bitmap.CompressFormat COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
    private final StorageWriter mStorageWriter;
    private final StorageRemover mStorageRemover;
    private final JournalFetcher mJournalFetcher;

    /**
     * Constructor.
     *
     * @param context         Preferably android application context.
     * @param journal         Journal to keep entries.
     * @param compressQuality Bitmap compress quality.
     */
    public Storage(Context context, Journal journal, int compressQuality) {
        FileSystem fileSystem = new FileSystem(context, compressQuality, COMPRESS_FORMAT);
        mStorageWriter = new StorageWriter(journal, fileSystem);
        mStorageRemover = new StorageRemover(journal, fileSystem);
        mJournalFetcher = new JournalFetcher(journal);
    }

    /**
     * Write to storage.
     *
     * @param filePath Original image path.
     * @param bitmap   Bitmap to write.
     * @return Entry of the result or null if fail.
     */
    public synchronized JournalEntry write(String filePath, Bitmap bitmap) {
        return mStorageWriter.write(filePath, bitmap);
    }

    /**
     * Remove from storage.
     *
     * @param entry Entry to remove.
     */
    public synchronized void remove(JournalEntry entry) {
        mStorageRemover.remove(entry);
    }

    /**
     * Fetch from storage.
     *
     * @return
     */
    public synchronized JournalEntry[] fetchAll() {
        return mJournalFetcher.fetchAll();
    }
}
