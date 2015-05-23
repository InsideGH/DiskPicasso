package com.sweetlab.diskpicasso.filesystem;

import android.graphics.Bitmap;

/**
 * A write request.
 */
public class WriteRequest {
    private final String mFileKey;
    private final Bitmap mBitmap;

    /**
     * Constructor.
     *
     * @param fileKey Unique source file key.
     * @param bitmap  Bitmap to compress and write.
     */
    public WriteRequest(String fileKey, Bitmap bitmap) {
        mFileKey = fileKey;
        mBitmap = bitmap;
    }

    public String getFileKey() {
        return mFileKey;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }
}
