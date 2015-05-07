package com.peter100.home.pablopicasso.storage;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.peter100.home.pablopicasso.CacheEntry;
import com.peter100.home.pablopicasso.executor.CacheExecutor;
import com.peter100.home.pablopicasso.filesystem.WriteRequest;
import com.peter100.home.pablopicasso.journal.Journal;
import com.peter100.home.pablopicasso.journal.realm.RealmJournal;

import java.io.File;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Disk cache using local application data storage location and journal of any choice.
 */
public class DiskCache {
    private final DiskReflection mDiskReflection;
    private final Storage mStorage;
    private final ReentrantReadWriteLock mRwLock;

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
         * @return
         */
        public Builder setCacheSize(int bytes) {
            mCacheSize = bytes;
            return this;
        }

        /**
         * Client can choose to implement own journal.
         *
         * @param journal
         * @return
         */
        public Builder setJournal(Journal journal) {
            mJournal = journal;
            return this;
        }

        /**
         * Set the compress quality.
         *
         * @param quality
         * @return
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
        mStorage = new Storage(context, journal, compressQuality);
        mDiskReflection = new DiskReflection(diskCacheBytes);
        mRwLock = new ReentrantReadWriteLock(true);
    }

    /**
     * Put a bitmap into the disk cache. Will execute in background.
     *
     * @param filePath The path to the original image.
     * @param bitmap   Bitmap to write/compress to disk cache.
     */
    public void put(final String filePath, Bitmap bitmap) {
        if (null == get(filePath, bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig())) {
            mStorage.write(new WriteRequest(filePath, bitmap)).subscribeOn(Schedulers.io())
                    .subscribe(new Action1<CacheEntry>() {
                        @Override
                        public void call(CacheEntry entry) {
                            mRwLock.writeLock().lock();
                            try {
                                mDiskReflection.put(entry.getIdentity(), entry);
                            } finally {
                                mRwLock.writeLock().unlock();
                            }
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
    public File get(String originalPath, int cachedWidth, int cachedHeight,
                    Bitmap.Config cachedConfig) {
        CacheEntry entry;
        long identity = CacheEntry
                .calcIdentity(originalPath, cachedWidth, cachedHeight, cachedConfig);
        mRwLock.readLock().lock();
        try {
            entry = mDiskReflection.get(identity);
            return entry != null ? entry.getCacheFile() : null;
        } finally {
            mRwLock.readLock().unlock();
        }
    }

    /**
     * Initialize the disk cache memory reflection from persisted storage.
     */
    /*package*/ void init() {
        CacheEntry[] entries = mStorage.fetchAll();
        if (entries != null) {
            mRwLock.writeLock().lock();
            try {
                for (CacheEntry e : entries) {
                    mDiskReflection.put(e.getIdentity(), e);
                }
            } finally {
                mRwLock.writeLock().unlock();
            }
        }
    }

    /**
     * This is a memory reflection of the disk cache and a disk size limiter.
     */
    private class DiskReflection extends LruCache<Long, CacheEntry> {
        private final CacheExecutor mExecutor;

        public DiskReflection(int maxSize) {
            super(maxSize);
            mExecutor = new CacheExecutor.Builder().setMax(2).build();
        }

        @Override
        protected int sizeOf(Long key, CacheEntry entry) {
            return entry.getByteSize();
        }

        @Override
        protected void entryRemoved(boolean evicted, Long key, final CacheEntry old,
                                    CacheEntry prev) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    mStorage.remove(old);
                }
            });
        }
    }
}