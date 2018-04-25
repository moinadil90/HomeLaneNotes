package my.homelane.app.utils;

import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by moinadil on 23/04/18.
 */

public class ImageProcessingUtil {
    private static final String TAG = ImageProcessingUtil.class.getSimpleName();
    private final ContentResolver mContentResolver;
    private final String filesDir;
    private final Application application;

    public ImageProcessingUtil(Application application) {
        this.application = application;
        this.mContentResolver = application.getContentResolver();
        this.filesDir = application.getFilesDir().getAbsolutePath();
    }

    /**
     * @param uri URI of image to be processed
     * @return Pair of Boolean and File
     *         Boolean - indicates whether returned File is temporary and has to be deleted after use
     *         File - is the File object of the image
     **/
    public Pair<Boolean, File> getProcessedImageFile(Uri uri) {
        Cursor cursor = mContentResolver.query(uri,
            new String[] { MediaStore.Images.ImageColumns.ORIENTATION},
            null, null, null);
        File processedFile;
        Boolean isTemp;

        if (cursor == null || cursor.getCount() != 1 || cursor.getColumnCount() == 0) {
            processedFile = new File(getFilePathFromUri(uri));
            isTemp = false;
        } else {
            cursor.moveToFirst();
            int rotation = cursor.getInt(0);
            cursor.close();
            Log.v("Kanj", "rotation of " + uri.getPath() + " = " + rotation);
            try {
                processedFile = getRotatedImageFile(uri, rotation);
                isTemp = true;
            } catch (IOException ioe) {
                Log.e(TAG, "Error", ioe);
                processedFile = new File(getFilePathFromUri(uri));
                isTemp = false;
            }
        }

        return new Pair<>(isTemp, processedFile);
    }

    private File getRotatedImageFile(Uri originalImage, int rotation) throws IOException {
        InputStream inputStream = mContentResolver.openInputStream(originalImage);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);

        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
            bitmap.getHeight(), matrix, true);

        File tempImageFile = File.createTempFile("_temp",
            ".jpg", new File(filesDir));

        FileOutputStream outputStream = new FileOutputStream(tempImageFile);

        // Compress method to format PNG is lossless but it will result in file larger than the original file.
        // It is important that this rotated image has smaller file size than the original because we have already
        // calculated that max upload size is not being exceeded based on original file sizes.
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);

        outputStream.flush();
        outputStream.close();

        Log.v("Moin", "rotated " + originalImage.getPath() + " by " + rotation + " to "
            + tempImageFile.getAbsolutePath());
        return tempImageFile;
    }

    public File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String imageFileName = "Property_image_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        storageDir.mkdirs();
        File image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
        );
        return image;
    }

    public String getFilePathFromUri(final Uri uri) {
        // Captured photo
        if (isCapturedPhoto(uri)) {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .getAbsolutePath()
                + "/" + uri.getLastPathSegment();
        }

        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
            && DocumentsContract.isDocumentUri(application.getApplicationContext(), uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                    split[1]
                };

                return getDataColumn(contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private String getDataColumn(Uri uri, String selection,
        String[] selectionArgs) {

        Cursor cursor = null;
        final String column = MediaStore.Images.Media.DATA;
        final String[] projection = {
            column
        };

        try {
            cursor = mContentResolver.query(uri, projection, selection, selectionArgs,
                null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }


    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }


    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    private static boolean isCapturedPhoto(Uri uri) {
        return "com.owners.buyer.fileprovider".equals(uri.getAuthority());
    }
}
