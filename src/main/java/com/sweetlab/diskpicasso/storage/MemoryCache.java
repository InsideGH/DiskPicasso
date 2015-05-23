package com.sweetlab.diskpicasso.storage;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.sweetlab.diskpicasso.CacheEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This is a memory cache for cache entries. The size of the case is based on the
 * cache entry sizes and not the cache entry count.
 */
public class MemoryCache {
    /**
     * Read write lock.
     */
    private final ReentrantReadWriteLock mMemoryLock;

    /**
     * Lru cache for limitation and exact retrieval.
     */
    private final CacheLimiter mCacheLimiter;

    /**
     * Map holding cache entries with original source path as key.
     */
    private final Map<String, List<CacheEntry>> mFileKeyListMap;

    /**
     * Listener for evictions.
     */
    private final EvictionListener mEvictionListener;

    /**
     * Constructor.
     *
     * @param cacheSizeBytes   Cache size in bytes.
     * @param evictionListener Eviction listener.
     */
    public MemoryCache(int cacheSizeBytes, EvictionListener evictionListener) {
        mMemoryLock = new ReentrantReadWriteLock(true);
        mCacheLimiter = new CacheLimiter(cacheSizeBytes);
        mFileKeyListMap = new HashMap<>();
        mEvictionListener = evictionListener;
    }

    /**
     * Get cache entry from memory cache.
     *
     * @param fileKey The source file key.
     * @param width   The wanted width of the cached image.
     * @param height  The wanted height of the cached image.
     * @param config  The wanted bitmap config of the cached image.
     * @return File representing the cache file or null of not found.
     */
    public File getExact(String fileKey, int width, int height, Bitmap.Config config) {
        long primaryKey = CacheEntry.calcPrimaryKey(fileKey, width, height, config);
        mMemoryLock.readLock().lock();
        try {
            CacheEntry entry = mCacheLimiter.get(primaryKey);
            return entry != null ? entry.getFile() : null;
        } finally {
            mMemoryLock.readLock().unlock();
        }
    }

    /**
     * Get all cache entries for given a source file key.
     *
     * @param fileKey The source file key.
     * @return Unmodifiable list of cache entries.
     */
    public List<CacheEntry> get(String fileKey) {
        List<CacheEntry> cacheEntries = mFileKeyListMap.get(fileKey);
        if (cacheEntries != null) {
            return Collections.unmodifiableList(mFileKeyListMap.get(fileKey));
        }
        return Collections.emptyList();
    }

    /**
     * Put a cache entry into the memory cache.
     *
     * @param entry The cache entry.
     */
    public void put(CacheEntry entry) {
        mMemoryLock.writeLock().lock();
        try {
            final String fileKey = entry.getFileKey();
            final long primaryKey = entry.getPrimaryKey();

            List<CacheEntry> entryList = mFileKeyListMap.get(fileKey);
            if (entryList == null) {
                entryList = new ArrayList<>();
                mFileKeyListMap.put(fileKey, entryList);
            }
            if (!entryList.contains(entry)) {
                entryList.add(entry);
            }
            mCacheLimiter.put(primaryKey, entry);
        } finally {
            mMemoryLock.writeLock().unlock();
        }
    }

    /**
     * Remove a cache entry from the memory cache.
     *
     * @param entry The entry to remove.
     */
    public void remove(CacheEntry entry) {
        mMemoryLock.writeLock().lock();
        try {
            final String fileKey = entry.getFileKey();
            final long primaryKey = entry.getPrimaryKey();

            List<CacheEntry> entryList = mFileKeyListMap.get(fileKey);
            if (entryList != null) {
                entryList.remove(entry);
            }
            mCacheLimiter.remove(primaryKey);
        } finally {
            mMemoryLock.writeLock().unlock();
        }
    }

    /**
     * Initialize the memory cache.
     *
     * @param entries Entries to initialize the cache with.
     */
    public void init(CacheEntry[] entries) {
        if (entries != null) {
            mMemoryLock.writeLock().lock();
            try {
                for (CacheEntry entry : entries) {
                    put(entry);
                }
            } finally {
                mMemoryLock.writeLock().unlock();
            }
        }
    }


    /**
     * Lru-cache for limiting the cache size.
     */
    private class CacheLimiter extends LruCache<Long, CacheEntry> {
        public CacheLimiter(int maxSize) {
            super(maxSize);
        }

        @Override
        protected int sizeOf(Long key, CacheEntry entry) {
            return entry.getByteSize();
        }

        @Override
        protected void entryRemoved(boolean evicted, Long key, final CacheEntry old, CacheEntry prev) {
            mEvictionListener.onEvicted(old);
//            Log.d("Peter100", "CacheLimiter.entryRemoved " + old);
        }
    }
}
