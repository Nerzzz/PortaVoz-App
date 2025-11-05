package com.example.portavoz;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class FeedActivity extends AppCompatActivity {

    ImageView userPfpImage;
    FeedAdapter adapter;
    RecyclerView timeLine;
    LinearLayoutManager linearLayoutManager;
    ArrayList<Post> posts = new ArrayList<>();
    String userData;
    String token;
    ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feed);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loading = findViewById(R.id.feed_loadingProgress);
        loading.setVisibility(VISIBLE);

        timeLine = findViewById(R.id.feed_recyTimeLine);
        linearLayoutManager = new LinearLayoutManager(this);
        timeLine.setLayoutManager(linearLayoutManager);

        userPfpImage = findViewById(R.id.feed_imgUser);

        userPfpImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FeedActivity.this, PersonalProfileActivity.class));
            }
        });

        getUserToken();
    }

    public void getUserToken(){

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    @Override
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if(task.isSuccessful()){
                            token = task.getResult().getToken();

                            GetUserByID gutid = new GetUserByID(token);
                            gutid.execute();

                            GetAllPosts gap = new GetAllPosts();
                            gap.execute();
                        }
                    }
                });
    }

    public class GetUserByID extends AsyncTask<String, Void, String>{

        HttpsURLConnection conn;

        String tok;
        public GetUserByID(String tok){
            this.tok = tok;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                String userId = mUser.getUid();

                URL url = new URL("https://portavoz.onrender.com/api/v1/users/"+userId);

                conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Authorization", "Bearer " + tok);
                conn.setDoInput(true);

                Log.v("USER_UID", userId);

                InputStream is = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                br.close();
                is.close();
                conn.disconnect();

                return sb.toString();

            } catch (ProtocolException e) {
                throw new RuntimeException(e);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                Log.e("API_ERROR_USER", "Erro ao acessar API", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String APIResult_user){
            if(APIResult_user != null){
                try {
                    Log.v("CURRENT_USER_DATA", APIResult_user);

                    JSONObject root = new JSONObject(APIResult_user);

                    JSONObject userObj = root.getJSONObject("user");
                    String userImage = userObj.getString("image");

                    Glide.with(FeedActivity.this)
                            .load(userImage)
                            .placeholder(R.drawable.user_image_placeholder)
                            .error(R.drawable.user_image_placeholder)
                            .into(userPfpImage);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public class GetAllPosts extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            //Log.v("TOKEN_DEBUG", "Bearer " + token);

            HttpsURLConnection conn;
            try {
                URL url = new URL("https://portavoz.onrender.com/api/v1/posts");

                conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setDoInput(true);

                InputStream is = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                br.close();
                is.close();
                conn.disconnect();

                return sb.toString();

            } catch (ProtocolException e) {
                throw new RuntimeException(e);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                Log.e("API_ERROR_POSTS", "Erro ao acessar API", e);
                return null;
            }
        }

        protected void onPostExecute(String APIResult_posts) {
            if (APIResult_posts != null) {
                Log.v("API_RESPONSE", APIResult_posts);

                try {
                    JSONObject root = new JSONObject(APIResult_posts);
                    JSONArray postsArray = root.getJSONArray("posts");

                    for (int i = 0; i < postsArray.length(); i++) {
                        JSONObject postObj = postsArray.getJSONObject(i);

                        String id_ = postObj.getString("_id");
                        JSONObject userObj = postObj.getJSONObject("user");
                        String username = userObj.getString("username");
                        String userId = userObj.getString("_id");
                        String userImage = userObj.getString("image");

                        String title = postObj.getString("title");
                        String desc = postObj.getString("desc");
                        String createdAt = postObj.getString("createdAt");
                        String updatedAt = postObj.getString("updatedAt");
                        String status = postObj.getString("status");
                        String address = postObj.getString("address");

                        int v__ = postObj.getInt("__v");
                        int likes = postObj.getInt("upvotesCount");
                        int comments = postObj.getInt("commentsCount");

                        boolean hasMore = root.has("hasMore") && root.getBoolean("hasMore");
                        boolean isUpvoted = postObj.has("isUpvoted") && postObj.getBoolean("isUpvoted");

                        // images
                        JSONArray imagesJson = postObj.getJSONArray("images");
                        List<String> images = new ArrayList<>();
                        for (int j = 0; j < imagesJson.length(); j++) {
                            images.add(imagesJson.getString(j));
                        }

                        // hashtags
                        JSONArray hashtagsJson = postObj.getJSONArray("hashtags");
                        List<String> hashtags = new ArrayList<>();
                        List<String> hId = new ArrayList<>();

                        for (int j = 0; j < hashtagsJson.length(); j++) {
                            JSONObject hashtagObj = hashtagsJson.getJSONObject(j);
                            String content = hashtagObj.optString("content", "");
                            String hashid = hashtagObj.optString("_id", "");
                            if (!content.isEmpty()) {
                                hashtags.add(content);
                            }
                            if(!hashid.isEmpty()){
                                hId.add(hashid);
                            }
                        }

                        // address
                        JSONObject location = postObj.getJSONObject("location");
                        double lat = location.getDouble("latitude");
                        double lon = location.getDouble("longitude");

                        // criar Post
                        posts.add(new Post(
                                userId,
                                username,
                                userImage,
                                id_,
                                title,
                                desc,
                                images,
                                status,
                                createdAt,
                                updatedAt,
                                lon,
                                lat,
                                address,
                                v__,
                                hashtags,
                                hId,
                                hasMore,
                                isUpvoted,
                                likes,
                                comments
                        ));
                    }

                    adapter = new FeedAdapter(posts, FeedActivity.this);
                    timeLine.setAdapter(adapter);

                    loading.setVisibility(INVISIBLE);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("API_RESPONSE", "Resposta nula da API");
            }
        }
    }
}