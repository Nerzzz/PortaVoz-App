package com.example.portavoz.post.comments;

import org.w3c.dom.Comment;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CommentService {
    @Multipart
    @POST("api/v1/posts/comments")
    Call<CommentResponse> createComment (
        @Header("Authorization") String token,
        @Part("parentId") RequestBody parentId,
        @Part("content") RequestBody content
    );

    @DELETE("api/v1/posts/comments/{commentId}")
    Call<CommentResponse> deleteComment (
            @Header("Authorization") String token,
            @Path("commentId") String commentId
    );

    @GET("api/v1/posts/{parentId}/comments")
    Call<CommentResponse> getComments (
            @Header("Authorization") String token,
            @Path("parentId") String parentId,
            @Query("page") int page
    );
}
