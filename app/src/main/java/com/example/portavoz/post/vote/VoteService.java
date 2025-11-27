package com.example.portavoz.post.vote;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;


public interface VoteService {
    @POST("v1/posts/{parentId}/upvote")
    Call<Void> createUpvote(
            @Header("Authorization") String token,
            @Path("parentId") String parentId
    );

    @DELETE("v1/posts/{parentId}/desupvote")
    Call<Void> deleteUpvote(
            @Header("Authorization") String token,
            @Path("parentId") String parentId
    );
}
