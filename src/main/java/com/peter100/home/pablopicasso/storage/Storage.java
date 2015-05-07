package com.peter100.home.pablopicasso.storage;

import android.content.Context;
import android.graphics.Bitmap;

import com.peter100.home.pablopicasso.CacheEntry;
import com.peter100.home.pablopicasso.filesystem.FileSystem;
import com.peter100.home.pablopicasso.filesystem.WriteRequest;
import com.peter100.home.pablopicasso.journal.Journal;

import java.io.File;
import java.io.IOException;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;

/**
 * Storage handling file and journal write, read and remove operation.
 */
public class Storage {
    private static final Bitmap.CompressFormat COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
    private final FileSystem mFileSystem;
    private final Journal mJournal;
    private final Object mGuard = new Object();

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

    public Observable<CacheEntry> write(final WriteRequest req) {
        return Observable.create(new OnSubscribe<CacheEntry>() {
            @Override
            public void call(Subscriber<? super CacheEntry> subscriber) {
                try {
                    CacheEntry entry;
                    synchronized (mGuard) {
                        File cacheFile = mFileSystem.write(req);
                        entry = createCacheEntry(req, cacheFile);
                        mJournal.insert(entry);
                    }
                    subscriber.onNext(entry);
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    /**
     * Remove from storage.
     *
     * @param entry Entry to remove.
     */
    public void remove(CacheEntry entry) {
        synchronized (mGuard) {
            mFileSystem.remove(entry);
            mJournal.remove(entry);
        }
    }

    /**
     * Fetch from storage.
     *
     * @return
     */
    public CacheEntry[] fetchAll() {
        synchronized (mGuard) {
            return mJournal.retrieveAll();
        }
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
        return new CacheEntry(path, cacheFile, bitmap.getWidth(), bitmap.getHeight(),
                bitmap.getConfig(), (int)cacheFile.length());
    }
}