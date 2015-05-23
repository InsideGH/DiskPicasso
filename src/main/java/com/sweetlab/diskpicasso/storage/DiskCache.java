package com.sweetlab.diskpicasso.storage;

import android.content.Context;
import android.graphics.Bitmap;

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
    private final MemoryCache mMemoryCache;

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
        mMemoryCache = new MemoryCache(diskCacheBytes, new MemoryCacheListener());
    }

    /**
     * Put a bitmap into the disk cache. Asynchronous call.
     *
     * @param fileKey The source file key.
     * @param bitmap  Bitmap to writeStorage/compress to disk cache.
     */
    public void put(final String fileKey, Bitmap bitmap) {
        if (null == getExact(fileKey, bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig())) {
            Observable<CacheEntry> observable = writeStorage(new WriteRequest(fileKey, bitmap)).subscribeOn(Schedulers.io());

            observable.subscribe(new Action1<CacheEntry>() {
                @Override
                public void call(CacheEntry entry) {
//                    Log.d("Peter100", "DiskCache.call put " + entry);
                    mMemoryCache.put(entry);
                }
            });
        }
    }

    /**
     * Get cached image from cache. Synchronous call.
     *
     * @param fileKey The source file key.
     * @param width   The width of the cached image.
     * @param height  The height of the cached image.
     * @param config  The bitmap config of the cached image.
     * @return A file referencing the cached image or null if no match.
     */
    public File getExact(String fileKey, int width, int height, Bitmap.Config config) {
        return mMemoryCache.getExact(fileKey, width, height, config);
    }

    /**
     * Get a list of cache entries for the given source file key.
     *
     * @param fileKey The source file key.
     * @return A unmodifiable list of entries.
     */
    public List<CacheEntry> get(String fileKey) {
        return mMemoryCache.get(fileKey);
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
            mMemoryCache.init(entries);
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
                        File file = mFileSystem.write(req);
                        entry = createEntry(req, file);
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
    private CacheEntry createEntry(WriteRequest req, File cacheFile) {
        final Bitmap bitmap = req.getBitmap();
        final String fileKey = req.getFileKey();
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        final Bitmap.Config config = bitmap.getConfig();
        final int byteSize = (int) cacheFile.length();
        return new CacheEntry(fileKey, cacheFile, width, height, config, byteSize);
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
                        mMemoryCache.remove(entry);
                        mFileSystem.remove(entry);
                        mJournal.remove(entry);
                    }
                }
            });
        }
    }
}