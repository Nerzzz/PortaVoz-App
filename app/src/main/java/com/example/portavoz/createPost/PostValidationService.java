package com.example.portavoz.createPost;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface PostValidationService {
    @Multipart
    @POST("api/v1/validate/{stage}")
    Call<ValidationResponse> validateStage(
            @Path("stage") String stage,
            @Part("title") RequestBody title,
            @Part("desc") RequestBody desc,
            @Part List<MultipartBody.Part> images,       // só para stage = images
            @Part List<MultipartBody.Part> hashtags      // só para stage = hashtags
    );
}
