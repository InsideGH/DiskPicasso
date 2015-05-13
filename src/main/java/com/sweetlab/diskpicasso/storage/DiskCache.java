package com.sweetlab.diskpicasso.storage;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.sweetlab.diskpicasso.CacheEntry;
import com.sweetlab.diskpicasso.filesystem.FileSystem;
import com.sweetlab.diskpicasso.filesystem.WriteRequest;
import com.sweetlab.diskpicasso.journal.Journal;
import com.sweetlab.diskpicasso.journal.realm.RealmJournal;

import java.io.File;
import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Disk cache using local application data storage location and journal of any choice.
 */
public class DiskCache {
    private static final Bitmap.CompressFormat COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
    private final Object mStorageGuard = new Object();
    private final FileSystem mFileSystem;
    private final Journal mJournal;
    private final CacheEntryCache mCacheEntryCache;

    /**
     * Builder to build a uninitialized cache.
     */
    public static class Builder {
        private int mCacheSize;
        private Journal mJournal;
        private int mQuality;

        /**
         * Default size if 200 MBytes and compress rate of 90.
         */
        public Builder() {
            mCacheSize = 200 * 1024 * 1024;
            mQuality = 90;
        }

        /**
         * Set specific disk cache size.
         *
         * @param bytes Size in bytes.
         * @return The builder.
         */
        public Builder setCacheSize(int bytes) {
            mCacheSize = bytes;
            return this;
        }

        /**
         * Client can choose to implement own journal.
         *
         * @param journal Journal to use.
         * @return The builder.
         */
        public Builder setJournal(Journal journal) {
            mJournal = journal;
            return this;
        }

        /**
         * Set the compress quality.
         *
         * @param quality Quality to compress with.
         * @return The builder.
         */
        public Builder setCompressQuality(int quality) {
            mQuality = quality;
            return this;
        }

        /**
         * Build an uninitialized cache.
         *
         * @param context Android application context preferably.
         * @return Uninitialized cache.
         */
        public UnInitializedCache build(Context context) {
            if (mJournal == null) {
                mJournal = new RealmJournal(context);
            }
            return new UnInitializedCache(new DiskCache(context, mCacheSize, mJournal, mQuality));
        }
    }

    /**
     * Private constructor to enforce initialization method.
     *
     * @param context        Android application context preferably.
     * @param diskCacheBytes Disk cache size in bytes.
     * @param journal        Journal used for persistence.
     */
    private DiskCache(Context context, int diskCacheBytes, Journal journal, int compressQuality) {
        mFileSystem = new FileSystem(context, compressQuality, COMPRESS_FORMAT);
        mJournal = journal;
        mCacheEntryCache = new CacheEntryCache(diskCacheBytes, new MemoryCacheListener());
    }

    /**
     * Put a bitmap into the disk cache. Asynchronous call.
     *
     * @param filePath The path to the original image.
     * @param bitmap   Bitmap to writeStorage/compress to disk cache.
     */
    public void put(final String filePath, Bitmap bitmap) {
        if (null == getExact(filePath, bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig())) {
            Observable<CacheEntry> observable = writeStorage(new WriteRequest(filePath, bitmap)).subscribeOn(Schedulers.io());

            observable.subscribe(new Action1<CacheEntry>() {
                @Override
                public void call(CacheEntry entry) {
                    mCacheEntryCache.put(entry);
                }
            });
        }
    }

    /**
     * Get cached image from cache. Synchronous call.
     *
     * @param originalPath The path to the original image.
     * @param cachedWidth  The width of the cached image.
     * @param cachedHeight The height of the cached image.
     * @param cachedConfig The bitmap config of the cached image.
     * @return A file referencing the cached image or null if no match.
     */
    public File getExact(String originalPath, int cachedWidth, int cachedHeight,
                         Bitmap.Config cachedConfig) {
        return mCacheEntryCache.getExact(originalPath, cachedWidth, cachedHeight, cachedConfig);
    }

    /**
     * Get a list of cache entries for the given source path.
     *
     * @param originalPath The source path.
     * @return A unmodifiable list of entries.
     */
    public List<CacheEntry> get(String originalPath) {
        return mCacheEntryCache.get(originalPath);
    }

    /**
     * Initialize the disk cache memory from persisted storage.
     */
    /*package*/ void init() {
        CacheEntry[] entries;
        synchronized (mStorageGuard) {
            entries = mJournal.retrieveAll();
        }
        if (entries != null) {
            Log.d("Peter100", "DiskCache.init");
            for (CacheEntry e : entries) {
                Log.d("Peter100", "DiskCache.init " + e);
            }
            mCacheEntryCache.init(entries);
        }
    }

    /**
     * Write to storage. If success onNext is called. If fails, onError is called.
     *
     * @param req Write request.
     * @return Observable of the writeStorage.
     */
    private Observable<CacheEntry> writeStorage(final WriteRequest req) {
        return Observable.create(new Observable.OnSubscribe<CacheEntry>() {
            @Override
            public void call(Subscriber<? super CacheEntry> subscriber) {
                try {
                    CacheEntry entry;
                    synchronized (mStorageGuard) {
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
     * Create a cache entry.
     *
     * @param req       Write request.
     * @param cacheFile The cache file to create.
     * @return The cache entry created.
     */
    private CacheEntry createCacheEntry(WriteRequest req, File cacheFile) {
        Bitmap bitmap = req.getBitmap();
        String path = req.getPath();
        return new CacheEntry(path, cacheFile, bitmap.getWidth(), bitmap.getHeight(),
                bitmap.getConfig(), (int) cacheFile.length());
    }

    /**
     * Memory cache eviction listener.
     */
    private class MemoryCacheListener implements EvictionListener {
        @Override
        public void onEvicted(final CacheEntry entry) {
            Schedulers.io().createWorker().schedule(new Action0() {
                @Override
                public void call() {
                    synchronized (mStorageGuard) {
                        mFileSystem.remove(entry);
                        mJournal.remove(entry);
                    }
                    mCacheEntryCache.remove(entry);
                }
            });
        }
    }
}