package com.example.portavoz.network;

import java.util.List;
import java.util.Map;

import retrofit2.http.Multipart;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;


public interface ApiService {
    @Multipart
    @PUT("api/v1/users/{id}")
    Call<UserResponse> updateUser(
            @Path("id") String id,
            //@Part("username") RequestBody username,
            //@Part("fName") RequestBody fName,
            //@Part("lName") RequestBody lName,
            //@Part("about") RequestBody about,
            @PartMap Map<String, RequestBody> fields,
            @Part MultipartBody.Part image,
            @Part MultipartBody.Part banner
    );
}