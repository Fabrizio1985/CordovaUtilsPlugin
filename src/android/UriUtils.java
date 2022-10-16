package android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

/**
 * https://stackoverflow.com/questions/72846731/how-to-get-content-uri-for-android-12-for-document-filespdf-doc-word
 */
public class UriUtils {
    private static Uri contentUri = null;
    private static MimeTypeMap mimeType = MimeTypeMap.getSingleton();
    
    private UriUtils() {
    	
    }

    @SuppressLint("NewApi")
    public static String getPathFromUri(final Context context, final String uriString) {
    	Log.i("Decode uri ===========", uriString);
    	
    	final Uri uri = Uri.parse(uriString);
    	
        // check here to is it KITKAT or new version
        final boolean isKitKatOrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        String selection = null;
        String[] selectionArgs = null;

        // DocumentProvider
        if (isKitKatOrAbove && DocumentsContract.isDocumentUri(context, uri)) {
        	Log.i("Is document ===========", uriString);

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                
            	final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");

                String fullPath = getPathFromExtSD(split);
                if (!"".equals(fullPath)) {
                    return fullPath;
                } else {
                    return null;
                }
            } else if (isDownloadsDocument(uri)) {
            	
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    final String id;
                    Cursor cursor = null;
                    try {
                        cursor = context.getContentResolver().query(uri, new String[]{MediaStore.MediaColumns.DISPLAY_NAME}, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            String fileName = cursor.getString(0);
                            String path = Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName;
                            if (!TextUtils.isEmpty(path)) {
                                return path;
                            }
                        }
                    } finally {
                        if (cursor != null)
                            cursor.close();
                    }
                    id = DocumentsContract.getDocumentId(uri);
                    if (!TextUtils.isEmpty(id)) {
                        if (id.startsWith("raw:")) {
                            return id.replaceFirst("raw:", "");
                        }
                        String[] contentUriPrefixesToTry = new String[]{
                                "content://downloads/public_downloads",
                                "content://downloads/my_downloads"
                        };
                        for (String contentUriPrefix : contentUriPrefixesToTry) {
                            try {
                                final Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.valueOf(id));
                                return getDataColumn(context, contentUri, null, null);
                            } catch (NumberFormatException e) {
                                //In Android 8 and Android P the id is not a number
                                return uri.getPath().replaceFirst("^/document/raw:", "").replaceFirst("^raw:", "");
                            }
                        }
                    }
                } else {
                    final String id = DocumentsContract.getDocumentId(uri);
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:", "");
                    }
                    try {
                        contentUri = ContentUris.withAppendedId( Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    if (contentUri != null) {
                        return getDataColumn(context, contentUri, null, null);
                    }
                }
            } else if (isMediaDocument(uri)) {
            	
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                selection = "_id=?";
                selectionArgs = new String[]{split[1]};

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                } 

                return getDataColumn(context, contentUri, selection, selectionArgs);
                
            } else if (isGoogleDriveUri(uri)) {
                return getDriveFilePath(uri, context);
            }
            
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
        	Log.i("Is Content ===========", uriString);
            
        	if (isGooglePhotosUri(uri)) {
        		Log.d("Is google photo", uriString);
        		
                return uri.getLastPathSegment();
            }
            if (isGoogleDriveUri(uri)) {
            	Log.d("Is google drive", uriString);
            	
                return getDriveFilePath(uri, context);
            }
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
            	Log.d("Is version N", uriString);
            	
                return getMediaFilePathForN(uri, context);
            } else {
            	Log.d("Else", uriString);
            	
            	String extracted = extractContent(uri, context);
            	
            	if(null != extracted) {
            		return extracted;
            	}
            	
                return copyFromSource(uri, context);
            }
            
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
        	Log.i("Is File ===========", uriString);
        	
        	return copyFromSource(uri, context);
        }
        
        return null;
    }
    
    private static String extractContent(Uri uri, Context context) {
    	ContentResolver cR = context.getContentResolver();
        Cursor cursor = null;
        try {
            String[] proj = { MediaColumns.DATA };
            cursor = cR.query(uri,  proj, null, null, null);
            int columnIndex = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
            cursor.moveToFirst();
            
            return cursor.getString(columnIndex);
            
        }  catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return null;
    }

    private static boolean fileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    private static String getPathFromExtSD(String[] pathData) {
        final String type = pathData[0];
        final String relativePath = "/" + pathData[1];
        String fullPath = "";

        if ("primary".equalsIgnoreCase(type)) {
            fullPath = Environment.getExternalStorageDirectory() + relativePath;
            if (fileExists(fullPath)) {
                return fullPath;
            }
        }

        fullPath = System.getenv("SECONDARY_STORAGE") + relativePath;
        if (fileExists(fullPath)) {
            return fullPath;
        }

        fullPath = System.getenv("EXTERNAL_STORAGE") + relativePath;
        if (fileExists(fullPath)) {
            return fullPath;
        }

        return fullPath;
    }

    private static String getDriveFilePath(Uri uri, Context context) {
        Uri returnUri = uri;
        Cursor returnCursor = context.getContentResolver().query(returnUri, null, null, null, null);

        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        File file = new File(context.getCacheDir(), name);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file.getPath();
    }

    private static String getMediaFilePathForN(Uri uri, Context context) {
        Uri returnUri = uri;
        Cursor returnCursor = context.getContentResolver().query(returnUri, null, null, null, null);

        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        File file = new File(context.getFilesDir(), name);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file.getPath();
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);

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

    private static String copyFromSource(Uri uri, Context context) {

        ContentResolver contentResolver = context.getContentResolver();
        String fileExtension = getFileExtension(uri, contentResolver);

        String fileName = queryName(uri, contentResolver);
        if (fileName == null)
            fileName = getFileName(fileExtension);

        // the file which will be the new cached file
        //File filePath = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File filePath = context.getExternalCacheDir();
        File outputFile = new File(filePath, fileName);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            //int bufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputFile.getAbsolutePath();
    }

    @SuppressLint("Recycle")
    private static String queryName(Uri uri, ContentResolver contentResolver) {
        Cursor returnCursor = contentResolver.query(uri, null, null, null, null);
        if (returnCursor == null)
            return null;

        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        if (nameIndex == -1)
            return null;

        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    private static String getFileExtension(Uri uri, ContentResolver contentResolver) {
        return mimeType.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private static String getFileName(String fileExtension) {
        return System.currentTimeMillis() + fileExtension + "";
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

    private static boolean isGoogleDriveUri(Uri uri) {
        return "com.google.android.apps.docs.storage".equals(uri.getAuthority()) || "com.google.android.apps.docs.storage.legacy".equals(uri.getAuthority());
    }
}
