package com.peter100.home.pablopicasso.storage;

import com.peter100.home.pablopicasso.JournalEntry;
import com.peter100.home.pablopicasso.journal.Journal;

/**
 * Fetch entries from a journal.
 */
public class JournalFetcher {
    private final Journal mJournal;

    /**
     * Constructor.
     *
     * @param journal Journal to fetch from.
     */
    public JournalFetcher(Journal journal) {
        mJournal = journal;
    }

    /**
     * Fetch all entries from the journal.
     *
     * @return
     */
    public JournalEntry[] fetchAll() {
        return mJournal.retrieveAll();
    }
}
