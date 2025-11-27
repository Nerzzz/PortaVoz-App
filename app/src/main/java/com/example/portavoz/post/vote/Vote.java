package com.example.portavoz.post.vote;

import com.example.portavoz.createPost.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Vote {
    private final VoteService api;

    public Vote() {
        api = RetrofitClient.getInstance().getVoteService();
    }

    public void createUpvote(String token, String parentId, VoteCallback callback) {
        api.createUpvote("Bearer " + token, parentId).enqueue(new Callback<Void>() {
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }

            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess();
                else callback.onError("Erro ao dar upvote");
            }
        });
    }

    public void deleteUpvote(String token, String parentId, VoteCallback callback) {
        api.deleteUpvote("Bearer " + token, parentId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess();
                else callback.onError("Erro ao remover upvote");
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public interface VoteCallback {
        void onSuccess();
        void onError(String error);
    }
}
