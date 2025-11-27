package com.example.portavoz.createPost;

import com.example.portavoz.post.vote.Vote;
import com.example.portavoz.post.vote.VoteService;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://portavoz.onrender.com";
    private static RetrofitClient instance = null;

    private PostService postService;
    private VoteService voteService;
    private PostValidationService postValidationService;

    private final OkHttpClient okHttpClient;

    private RetrofitClient() {

        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        postService = retrofit.create(PostService.class);
        postValidationService = retrofit.create(PostValidationService.class);
        voteService = retrofit.create(VoteService.class);
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public PostService getPostService() {
        return postService;
    }

    public PostValidationService getValidationService() {
        return postValidationService;
    }

    public VoteService getVoteService() { return voteService; }


}
