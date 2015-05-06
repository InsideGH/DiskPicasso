package com.peter100.home.pablopicasso.journal;

import com.peter100.home.pablopicasso.JournalEntry;

/**
 * Journal of cache entries.
 */
public interface Journal {
    /**
     * Insert entry into journal.
     *
     * @param entry
     */
    void insert(JournalEntry entry);

    /**
     * Remove entry from journal.
     *
     * @param entry
     */
    void remove(JournalEntry entry);

    /**
     * Retrieve all entries from journal.
     *
     * @return
     */
    JournalEntry[] retrieveAll();

    /**
     * Check if entry exists in the journal.
     *
     * @param identity
     * @return
     */
    boolean exists(long identity);
}
