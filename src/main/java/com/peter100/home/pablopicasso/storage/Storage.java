package com.peter100.home.pablopicasso.storage;

import android.content.Context;
import android.graphics.Bitmap;

import com.peter100.home.pablopicasso.CacheEntry;
import com.peter100.home.pablopicasso.filesystem.FileSystem;
import com.peter100.home.pablopicasso.filesystem.WriteRequest;
import com.peter100.home.pablopicasso.journal.Journal;

import java.io.File;

/**
 * Storage handling file and journal write, read and remove operation.
 */
public class Storage {
    private static final Bitmap.CompressFormat COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
    private final FileSystem mFileSystem;
    private final Journal mJournal;

    /**
     * Constructor.
     *
     * @param context         Preferably android application context.
     * @param journal         Journal to keep entries.
     * @param compressQuality Bitmap compress quality.
     */
    public Storage(Context context, Journal journal, int compressQuality) {
        mJournal = journal;
        mFileSystem = new FileSystem(context, compressQuality, COMPRESS_FORMAT);
    }

    /**
     * Write to storage.
     *
     * @param req
     * @return Entry of the result or null if fail.
     */
    public synchronized CacheEntry write(WriteRequest req) {
        File cacheFile = mFileSystem.write(req);
        if (cacheFile != null) {
            CacheEntry entry = createCacheEntry(req, cacheFile);
            mJournal.insert(entry);
            return entry;
        }
        return null;
    }

    /**
     * Remove from storage.
     *
     * @param entry Entry to remove.
     */
    public synchronized void remove(CacheEntry entry) {
        mFileSystem.remove(entry);
        mJournal.remove(entry);
    }

    /**
     * Fetch from storage.
     *
     * @return
     */
    public synchronized CacheEntry[] fetchAll() {
        return mJournal.retrieveAll();
    }

    /**
     * Create a cache entry.
     *
     * @param req
     * @param cacheFile
     * @return
     */
    private CacheEntry createCacheEntry(WriteRequest req, File cacheFile) {
        Bitmap bitmap = req.getBitmap();
        String path = req.getPath();
        return new CacheEntry(path, cacheFile, bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig(), (int) cacheFile.length());
    }
}
