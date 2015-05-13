package com.sweetlab.diskpicasso.filesystem;

import android.content.Context;
import android.graphics.Bitmap;

import com.sweetlab.diskpicasso.CacheEntry;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * File system to write and remove files from.
 */
public class FileSystem {
    private final String mCacheRootPath;
    private final int mQuality;
    private final Bitmap.CompressFormat mCompressFormat;

    /**
     * Constructor.
     *
     * @param context         Android application context.
     * @param compressQuality Bitmap compress quality.
     * @param format          Bitmap compress format.
     */
    public FileSystem(Context context, int compressQuality, Bitmap.CompressFormat format) {
        mQuality = compressQuality;
        mCompressFormat = format;
        mCacheRootPath = context.getCacheDir().getAbsolutePath();
    }

    /**
     * Create a new file and write bitmap into it.
     *
     * @param req Write request.
     * @return Journal entry of the result or null if fail.
     */
    public File write(WriteRequest req) throws IOException {
        String path = req.getPath();
        Bitmap bitmap = req.getBitmap();
        return createImageFile(path, bitmap, mCompressFormat, mQuality);
    }

    /**
     * Remove/delete a file based on journal entry information.
     *
     * @param entry Entry to remove.
     * @return True if success.
     */
    public boolean remove(CacheEntry entry) {
        File file = entry.getCacheFile();
        return file.delete();
    }

    /**
     * Create a new file. The file naming will include unique identity.
     *
     * @param filePath Original image path.
     * @param src      Bitmap to compress and write.
     * @param format   Bitmap compress format.
     * @param quality  Bitmap compress quality.
     * @return The image file containing compressed bitmap.
     */
    private File createImageFile(String filePath, Bitmap src, Bitmap.CompressFormat format,
                                 int quality) throws IOException {
        long identity = CacheEntry
                .calcIdentity(filePath, src.getWidth(), src.getHeight(), src.getConfig());
        File file = createFile(filePath + identity);
        writeFile(file, src, format, quality);
        return file;
    }

    /**
     * Create file. Any directory tree structure will be created if needed. Exception is thrown
     * if fail.
     *
     * @param filePath Path including any directories.
     * @return The file.
     */
    private File createFile(String filePath) throws IOException {
        File file = new File(mCacheRootPath + filePath);
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    /**
     * Write bitmap to file. Exception is thrown if fails.
     *
     * @param file    The cache file.
     * @param src     The bitmap.
     * @param format  Bitmap compress format.
     * @param quality Bitmap compress quality.
     */
    private void writeFile(File file, Bitmap src, Bitmap.CompressFormat format,
                           int quality) throws IOException {
        BufferedOutputStream stream = null;
        try {
            stream = new BufferedOutputStream(new FileOutputStream(file));
            src.compress(format, quality, stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }
}