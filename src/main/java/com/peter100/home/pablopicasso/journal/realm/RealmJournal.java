package com.peter100.home.pablopicasso.journal.realm;

import android.content.Context;
import android.graphics.Bitmap;

import com.peter100.home.pablopicasso.journal.Journal;
import com.peter100.home.pablopicasso.JournalEntry;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Journal implementation using Realm. Need a bit of cleanup.
 */
public class RealmJournal implements Journal {
    private static final String IDENTITY_KEY = "identity";
    private static final boolean DEBUG = true;
    private final Context mContext;

    /**
     * Constructor.
     *
     * @param context
     */
    public RealmJournal(Context context) {
        mContext = context;
    }

    @Override
    public void insert(JournalEntry entry) {
        if (!exists(entry.getIdentity())) {
            Realm realm = Realm.getInstance(mContext);
            realm.beginTransaction();

            RealmEntry realmEntry = realm.createObject(RealmEntry.class);
            realmEntry.setSourceFilePath(entry.getOriginalFilePath());
            realmEntry.setCacheFile(entry.getCacheFile().getAbsolutePath());
            realmEntry.setWidth(entry.getWidth());
            realmEntry.setHeight(entry.getHeight());
            realmEntry.setByteSize(entry.getByteSize());
            realmEntry.setIdentity(entry.getIdentity());
            realmEntry.setBitmapConfig(entry.getConfig().name());

            realm.commitTransaction();
            realm.close();
        }
    }

    @Override
    public void remove(JournalEntry entry) {
        Realm realm = Realm.getInstance(mContext);
        realm.beginTransaction();

        RealmResults<RealmEntry> result = realm.where(RealmEntry.class).equalTo(IDENTITY_KEY, entry.getIdentity()).findAll();
        if (DEBUG) {
            if (result.size() == 0) {
                throw new RuntimeException("remove : noting found");
            }
            if (result.size() > 1) {
                throw new RuntimeException("remove : multiple found " + result.size());
            }
        }
        result.clear();

        realm.commitTransaction();
        realm.close();
    }

    @Override
    public JournalEntry[] retrieveAll() {
        Realm realm = Realm.getInstance(mContext);

        RealmResults<RealmEntry> realmEntries = realm.allObjects(RealmEntry.class);
        int size = realmEntries.size();
        JournalEntry[] result = new JournalEntry[size];

        for (int i = 0; i < size; i++) {
            RealmEntry pabloEntry = realmEntries.get(i);
            result[i] = new JournalEntry(pabloEntry.getSourceFilePath(), new File(pabloEntry.getCacheFile()), pabloEntry.getWidth(), pabloEntry.getHeight(), createConfig(pabloEntry.getBitmapConfig()), pabloEntry.getByteSize());
        }

        realm.close();
        return result;
    }

    @Override
    public boolean exists(long identity) {
        Realm realm = Realm.getInstance(mContext);
        RealmResults<RealmEntry> list = realm.where(RealmEntry.class).equalTo(IDENTITY_KEY, identity).findAll();
        int size = list.size();
        realm.close();
        if (size == 1) {
            return true;
        } else if (size > 1 && DEBUG) {
            throw new RuntimeException("multiple id's found " + size);
        }
        return false;
    }

    private Bitmap.Config createConfig(String textConfig) {
        Bitmap.Config[] values = Bitmap.Config.values();
        for (Bitmap.Config config : values) {
            if (config.name().equals(textConfig)) {
                return config;
            }
        }
        throw new RuntimeException("Unknown bitmap config " + textConfig);
    }
}
