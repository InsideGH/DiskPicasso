package com.sweetlab.diskpicasso.storage;

import com.sweetlab.diskpicasso.CacheEntry;

/**
 * Memory cache listener.
 */
public interface EvictionListener {
    /**
     * Called when en entry is evicted from memory cache.
     *
     * @param entry The entry evicted.
     */
    void onEvicted(CacheEntry entry);
}
