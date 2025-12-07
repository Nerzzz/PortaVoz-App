package com.example.portavoz.createPost;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.Objects;

public class PostResponse {
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
