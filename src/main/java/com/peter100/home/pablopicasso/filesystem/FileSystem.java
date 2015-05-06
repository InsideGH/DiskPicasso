package com.peter100.home.pablopicasso.filesystem;

import android.content.Context;
import android.graphics.Bitmap;

import com.peter100.home.pablopicasso.JournalEntry;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
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
     * @param sourceFilePath Original image source path.
     * @param bitmap         Bitmap to write.
     * @return Journal entry of the result or null if fail.
     */
    public JournalEntry write(String sourceFilePath, Bitmap bitmap) {
        File cacheFile = null;
        try {
            cacheFile = createImageFile(sourceFilePath, bitmap, mCompressFormat, mQuality);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (cacheFile != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Bitmap.Config config = bitmap.getConfig();
            return new JournalEntry(sourceFilePath, cacheFile, width, height, config, (int) cacheFile.length());
        }
        return null;
    }

    /**
     * Remove/delete a file based on journal entry information.
     *
     * @param entry
     */
    public void remove(JournalEntry entry) {
        File file = entry.getCacheFile();
        file.delete();
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
    private File createImageFile(String filePath, Bitmap src, Bitmap.CompressFormat format, int quality) throws IOException {
        long identity = JournalEntry.calcIdentity(filePath, src.getWidth(), src.getHeight(), src.getConfig());
        File file = createFile(filePath + identity);
        writeFile(file, src, format, quality);
        return file;
    }

    /**
     * Create file. Any directory tree structure will be created if needed. Exception is thrown if fail.
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
    private void writeFile(File file, Bitmap src, Bitmap.CompressFormat format, int quality) throws FileNotFoundException {
        BufferedOutputStream stream = null;
        try {
            stream = new BufferedOutputStream(new FileOutputStream(file));
            src.compress(format, quality, stream);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
