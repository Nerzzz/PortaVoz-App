package com.example.portavoz.createPost;

import android.content.Context;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtils {
    public static File uriToFile(Context context, Uri uri) {
        try {
            String fileName = getFileName(context, uri);
            File file = new File(context.getCacheDir(), fileName);

            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buf = new byte[1024];
            int len;

            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }

            outputStream.close();
            inputStream.close();

            return file;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getFileName(Context context, Uri uri) {
        String result = null;
        try (android.database.Cursor cursor =
                     context.getContentResolver().query(uri, null, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                int column = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                result = cursor.getString(column);
            }
        }
        return (result != null) ? result : "temp_image.jpg";
    }
}
