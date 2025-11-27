package com.example.portavoz.post.comments;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.http.Multipart;
import retrofit2.http.POST;

public class CommentResponse {
    @SerializedName("comments")
    private List<Comment> comments;

    @SerializedName("hasMore")
    private boolean hasMore;

    public List<Comment> getComments() {
        return comments;
    }

    public boolean hasMore() {
        return hasMore;
    }
}
