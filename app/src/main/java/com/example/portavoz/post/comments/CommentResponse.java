package com.example.portavoz.post.comments;

import com.google.gson.annotations.SerializedName;

import retrofit2.http.Multipart;
import retrofit2.http.POST;

public class CommentResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Object data;

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}
