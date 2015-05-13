package com.sweetlab.diskpicasso.storage;

/**
 * Enforce client to take decision how to initialize the cache.
 */
public class UnInitializedCache {
    private final DiskCache mCache;

    /**
     * Hold uninitialized cache.
     *
     * @param cache The uninitialized cache.
     */
    public UnInitializedCache(DiskCache cache) {
        mCache = cache;
    }

    /**
     * Get the uninitialized cache.
     *
     * @return The uninitialized cache.
     */
    public DiskCache get() {
        return mCache;
    }

    /**
     * Initialize the cache from any thread.
     */
    public void init() {
        mCache.init();
    }
}
