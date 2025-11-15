package com.example.portavoz.network;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class Utils {

    public static RequestBody createTextPart(String value) {
        return RequestBody.create(
                MediaType.parse("text/plain"),
                value == null ? "" : value
        );
    }

    public static MultipartBody.Part createFilePart(String fieldName, File file) {
        if (file == null) return null;

        RequestBody reqFile = RequestBody.create(
                MediaType.parse("image/*"),
                file
        );

        return MultipartBody.Part.createFormData(fieldName, file.getName(), reqFile);
    }
}