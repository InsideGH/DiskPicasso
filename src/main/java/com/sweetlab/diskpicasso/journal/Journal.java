package com.sweetlab.diskpicasso.journal;

import com.sweetlab.diskpicasso.CacheEntry;

/**
 * Journal of cache entries.
 */
public interface Journal {
    /**
     * Insert entry into journal.
     *
     * @param entry Cache entry.
     */
    void insert(CacheEntry entry);

    /**
     * Remove entry from journal.
     *
     * @param entry Cache entry.
     */
    void remove(CacheEntry entry);

    /**
     * Retrieve all entries from journal.
     *
     * @return All cache entries.
     */
    CacheEntry[] retrieveAll();

    /**
     * Check if entry exists in the journal.
     *
     * @param identity Identity to search for.
     * @return True if found.
     */
    boolean exists(long identity);
}
