package com.sweetlab.diskpicasso.filesystem;

import android.graphics.Bitmap;

public class WriteRequest {
    private final String mFilePath;
    private final Bitmap mBitmap;

    public WriteRequest(String filePath, Bitmap bitmap) {
        mFilePath = filePath;
        mBitmap = bitmap;
    }

    public String getPath() {
        return mFilePath;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }
}
