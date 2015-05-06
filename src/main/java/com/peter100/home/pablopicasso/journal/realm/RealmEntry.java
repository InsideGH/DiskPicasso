package com.peter100.home.pablopicasso.journal.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Entry into Realm database.
 */
public class RealmEntry extends RealmObject {
    @PrimaryKey
    private long identity;

    private int width;
    private int height;
    private String bitmapConfig;
    private String cacheFile;
    private int byteSize;
    private String sourceFilePath;

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setBitmapConfig(String bitmapConfig) {
        this.bitmapConfig = bitmapConfig;
    }

    public void setCacheFile(String cacheFile) {
        this.cacheFile = cacheFile;
    }

    public void setByteSize(int byteSize) {
        this.byteSize = byteSize;
    }

    public void setIdentity(long identity) {
        this.identity = identity;
    }

    public void setSourceFilePath(String sourceFilePath) {
        this.sourceFilePath = sourceFilePath;
    }

    public String getSourceFilePath() {
        return sourceFilePath;
    }

    public String getCacheFile() {
        return cacheFile;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getBitmapConfig() {
        return bitmapConfig;
    }

    public int getByteSize() {
        return byteSize;
    }

    public long getIdentity() {
        return identity;
    }
}
