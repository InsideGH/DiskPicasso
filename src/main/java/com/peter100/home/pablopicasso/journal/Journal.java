package com.peter100.home.pablopicasso.journal;

import com.peter100.home.pablopicasso.CacheEntry;

/**
 * Journal of cache entries.
 */
public interface Journal {
    /**
     * Insert entry into journal.
     *
     * @param entry
     */
    void insert(CacheEntry entry);

    /**
     * Remove entry from journal.
     *
     * @param entry
     */
    void remove(CacheEntry entry);

    /**
     * Retrieve all entries from journal.
     *
     * @return
     */
    CacheEntry[] retrieveAll();

    /**
     * Check if entry exists in the journal.
     *
     * @param identity
     * @return
     */
    boolean exists(long identity);
}
