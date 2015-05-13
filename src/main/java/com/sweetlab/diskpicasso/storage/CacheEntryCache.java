package com.sweetlab.diskpicasso.storage;

import android.graphics.Bitmap;
import android.util.Log;
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
public class CacheEntryCache {
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
    private final Map<String, List<CacheEntry>> mCacheMap;

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
    public CacheEntryCache(int cacheSizeBytes, EvictionListener evictionListener) {
        mMemoryLock = new ReentrantReadWriteLock(true);
        mCacheLimiter = new CacheLimiter(cacheSizeBytes);
        mCacheMap = new HashMap<>();
        mEvictionListener = evictionListener;
    }

    /**
     * Get cache entry from memory cache.
     *
     * @param originalPath The source path.
     * @param cachedWidth  The wanted width of the cached image.
     * @param cachedHeight The wanted height of the cached image.
     * @param cachedConfig The wanted bitmap config of the cached image.
     * @return File representing the cache file or null of not found.
     */
    public File getExact(String originalPath, int cachedWidth, int cachedHeight,
                         Bitmap.Config cachedConfig) {
        long identity = CacheEntry.calcIdentity(originalPath, cachedWidth, cachedHeight, cachedConfig);
        mMemoryLock.readLock().lock();
        try {
            CacheEntry entry = mCacheLimiter.get(identity);
            return entry != null ? entry.getCacheFile() : null;
        } finally {
            mMemoryLock.readLock().unlock();
        }
    }

    /**
     * Get all cache entries for given file path.
     *
     * @param originalPath The original file path.
     * @return Unmodifiable list of cache entries.
     */
    public List<CacheEntry> get(String originalPath) {
        List<CacheEntry> cacheEntries = mCacheMap.get(originalPath);
        if (cacheEntries != null) {
            return Collections.unmodifiableList(mCacheMap.get(originalPath));
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
            Log.d("Peter100", "CacheEntryCache.put " + entry);
            List<CacheEntry> cacheEntries = mCacheMap.get(entry.getOriginalFilePath());
            if (cacheEntries == null) {
                cacheEntries = new ArrayList<>();
                mCacheMap.put(entry.getOriginalFilePath(), cacheEntries);
            }
            if (!cacheEntries.contains(entry)) {
                cacheEntries.add(entry);
            }
            mCacheLimiter.put(entry.getIdentity(), entry);
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
            List<CacheEntry> reflectionEntries = mCacheMap.get(entry.getOriginalFilePath());
            if (reflectionEntries != null) {
                reflectionEntries.remove(entry);
            }
            mCacheLimiter.remove(entry.getIdentity());
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
                for (CacheEntry e : entries) {
                    put(e);
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
            Log.d("Peter100", "CacheLimiter.entryRemoved " + old);
        }
    }
}
