# DiskPicasso

This is a local diskcache based on/using the [Picasso library](https://github.com/square/picasso).

Cached images files are stored into Android application
data folder. A journal of cache entries are stored
in either a sql or realm based journal implementation.

The storage which consists of a file (cache file - image)
and a entry in a journal (sql or realm) is using
RxJava to asynchronously perform writes. As well as a
cache entry cache with a map from a specific file path
to many cache entries (that vary in width, heigh or
bitmap config).

Getting stuff from disk is done by using a reflection
of the persisted items through a cache entry cache. 
The cache entry cache actually also helps to limit the 
disk cache size.

It's uses the picasso transformation to intercept the
bitmap placement on the ImageView to make a cache
entry.

Using the original source path with additional
parameters such as desired with, height and bitmap
config, the cache can be queried to check if 
cached version exists or not.

Example of usage would be like ->

    private static final Config BITMAP_CONFIG = Config.RGB_565;

    private void loadPhoto(LocalPhoto photoInfo, ImageView imageView) {
        final int width = imageView.getMeasuredWidth();
        final int height = imageView.getMeasuredHeight();

        DiskPicasso instance = DiskPicasso.getInstance();
        RequestCreator cacheLoader = instance.loadUsingCache(photoInfo.getFileKey(), width, height, BITMAP_CONFIG);

        if (cacheLoader != null) {
            // load the cache file with picasso. 
            cacheLoader.into(imageView);
        } else {
            // load source file with picasso and then do a DiskCache write.
            instance.loadAndWrite(photo.getSourcePath(), photo.getFileKey(), BITMAP_CONFIG).fit().centerCrop().into(imageView);
        }
    }


Another way to use it would be to request loading with a given resize dimension
instead if that is wished.

Then it's possible to query the cache for all entries for a given source path.
The client could then select which entry would fit best or skip.

if either the width or the height is know, there is a early implementation of a
compare (match) to use.


Example of usage would be like ->

    private void loadPhotoView(final PhotoMeta photo, PhotoViewHolder holder) {
        final AspectImageView imageView = holder.getImageView();
        final DiskPicasso instance = DiskPicasso.getInstance();
        
        // Get all entries for a given source file key.
        List<CacheEntry> cacheEntries = instance.getFromCache(photo.getFileKey());

        final int resizeX = calcPicassoResizeX(photo);
        final int resizeY = calcPicassoResizeY(photo);
        CacheEntry match = DiskPicasso.findMatch(cacheEntries, resizeX, resizeY, JPEG_CONFIG);
        if (match != null) {
	    // load the cache file with picasso.
            SinglePicasso.getPicasso().load(match.getFile()).into(imageView);
        } else {
            // load source file with picasso and then do a DiskCache write.
            instance.loadAndWrite(photo.getSourcePath(), photo.getFileKey(), JPEG_CONFIG).resize(resizeX, resizeY).into(imageView);
        }
    }

The file key is upto client to decide upon. Using the _ID column from Android MediaStore is one way
to add uniqueness to the original source path by merging these together.

Like this ->

    public static PhotoMeta createPhotoMeta(Cursor cursor) {
        final long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID));
        final String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
        final int width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.WIDTH));
        final int height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.HEIGHT));
        final int orientation = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION));
        final long dateTaken = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN));

        String fileKey = path + "_" + id;

        return new PhotoMeta(path, fileKey, width, height, orientation, dateTaken);
    }

