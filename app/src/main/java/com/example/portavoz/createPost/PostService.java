package com.example.portavoz.createPost;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

import java.util.List;

public interface PostService {
    @Multipart
    @POST("api/v1/posts")
    Call<PostResponse> createPost(
            @Header("Authorization") String token,
            @Part("title") RequestBody title,
            @Part("desc") RequestBody desc,
            @Part List<MultipartBody.Part> hashtags,
            @Part("location[latitude]") RequestBody latitude,
            @Part("location[longitude]") RequestBody longitude,
            @Part("address") RequestBody address,
            @Part("status") RequestBody status,
            @Part List<MultipartBody.Part> images
    );

    @DELETE("api/v1/posts/{postId}")
    Call<DeleteResponse> deletePost (
        @Header("Authorization") String token,
        @Path("postId") String postId
    );

}