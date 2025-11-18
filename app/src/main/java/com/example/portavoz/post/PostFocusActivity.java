package com.example.portavoz.post;

import static android.view.View.INVISIBLE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.portavoz.FeedActivity;
import com.example.portavoz.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class PostFocusActivity extends AppCompatActivity {

    Post userPost;

    MaterialButton btnReturn, btnLike, btnComment;
    ImageButton btnMap, btnOptions;
    TextView txtDisplayName, txtDate, txtTitle, txtDesc, txtTags;
    RecyclerView rcImages;
    ImageView imgUserPost, imgCurrentUser;
    ConstraintLayout post;
    ProgressBar loadingPost;

    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_activity_focus);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String postId = intent.getStringExtra("postId");

        post = findViewById(R.id.post);
        post.setVisibility(View.INVISIBLE);

        loadingPost = findViewById(R.id.progress_loadingPost);

        btnReturn = findViewById(R.id.post_btnReturn);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnLike = findViewById(R.id.post_btnLike);
        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnLike.setActivated(false);
                btnLike.setAlpha(0.6f);

                mUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    @Override
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if(task.isSuccessful()){

                        }
                    }
                });
            }
        });

        btnComment = findViewById(R.id.post_btnComment);

        btnMap = findViewById(R.id.post_btnMap);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context ctx = v.getContext();
                View dialogView = LayoutInflater.from(ctx).inflate(R.layout.post_layout_map, null);

                TextView txtAdress = dialogView.findViewById(R.id.map_txtAdress);
                MapView mapView = dialogView.findViewById(R.id.map_mapView);

                txtAdress.setText(userPost.address);

                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                dialog.show();

                mapView.onCreate(null);
                mapView.getMapAsync(googleMap -> {
                    LatLng koordinaty = new LatLng(userPost.lat, userPost.lon);

                    googleMap.addMarker(new MarkerOptions()
                            .position(koordinaty)
                            .title("Localização")
                            .snippet(userPost.address));

                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(koordinaty, 15f));
                });

                mapView.onResume();
            }
        });

        btnOptions = findViewById(R.id.post_btnOptions);

        txtDisplayName = findViewById(R.id.post_txtUsername);
        txtDate = findViewById(R.id.post_txtPostDate);
        txtTitle = findViewById(R.id.post_txtTitle);
        txtDesc = findViewById(R.id.post_txtDesc);
        txtTags = findViewById(R.id.post_txtHastags);

        rcImages = findViewById(R.id.post_carousel);
        imgUserPost = findViewById(R.id.post_imgUser);
        imgCurrentUser = findViewById(R.id.post_imgCurrentUser);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    @Override
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if(task.isSuccessful()){
                            String token = task.getResult().getToken();
                            new GetCurrentUser(token, postId, mUser.getUid()).execute();
                            new GetUserPost(token, postId).execute();
                        }
                    }
                });
    }

    public class GetCurrentUser extends AsyncTask<String, Void, String>{
        String token, postId, userUid;
        public GetCurrentUser(String token, String postId, String userUid){
            this.token = token;
            this.postId = postId;
            this.userUid = userUid;
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpsURLConnection conn;
            try {
                URL url = new URL("https://portavoz.onrender.com/api/v1/users/"+userUid);

                conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setDoInput(true);

                Log.v("USER_UID", userUid);

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

            } catch (ProtocolException | MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                Log.e("API_ERROR_USER", "Erro ao acessar API", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if(s != null){
                try {
                    JSONObject root = new JSONObject(s);

                    JSONObject userObj = root.getJSONObject("user");
                    String userImage = userObj.getString("image");

                    Glide.with(PostFocusActivity.this)
                            .load(userImage)
                            .placeholder(R.drawable.user_image_placeholder)
                            .error(R.drawable.user_image_placeholder)
                            .into(imgCurrentUser);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    public class GetUserPost extends AsyncTask<String, Void, String>{
        String token, postId;
        public GetUserPost(String token, String postId){
            this.token = token;
            this.postId = postId;
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpsURLConnection conn;
            try {
                URL url = new URL("https://portavoz.onrender.com/api/v1/posts/"+postId);

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

        @SuppressLint("ResourceAsColor")
        @Override
        protected void onPostExecute(String s) {
            if (s != null) {
                Log.v("API RESPONSE", s);

                try {
                    JSONObject root = new JSONObject(s);
                    JSONObject postObj = root.getJSONObject("post");

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

                    if(isUpvoted){
                        Log.v("ISUPVOTED", String.valueOf(isUpvoted));
                    }
                    else{
                        Log.v("ISUPVOTED", String.valueOf(isUpvoted));
                    }

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
                        hashtags.add(hashtagsJson.getString(j));
                        hId.add(hashtagsJson.getString(j)); // só pra subrir a falta
                    }

                    // address
                    JSONObject location = postObj.getJSONObject("location");
                    double lat = location.getDouble("latitude");
                    double lon = location.getDouble("longitude");

                    // criar Post
                    userPost = new Post(
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
                    );

                    txtDisplayName.setText(userPost.username);
                    txtDate.setText(userPost.created);

                    txtTitle.setText(userPost.title);
                    txtDesc.setText(userPost.desc);

                    txtTags.setText("#" + String.join(" #", userPost.hashtags));

                    btnLike.setText(String.valueOf(userPost.likes));
                    btnComment.setText(String.valueOf(userPost.comments));

                    Glide.with(PostFocusActivity.this)
                            .load(userPost.userImage)
                            .placeholder(R.drawable.user_image_placeholder)
                            .error(R.drawable.user_image_placeholder)
                            .into(imgUserPost);

                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(PostFocusActivity.this, LinearLayoutManager.HORIZONTAL, false);
                    rcImages.setLayoutManager(linearLayoutManager);
                    rcImages.setAdapter(new PostImageAdapter(userPost.images));

                    PagerSnapHelper snapHelper = new PagerSnapHelper();
                    if (rcImages.getOnFlingListener() != null) {
                        rcImages.setOnFlingListener(null);
                    }
                    snapHelper.attachToRecyclerView(rcImages);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                if(userPost.isUpvoted){
                    btnLike.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(PostFocusActivity.this, R.color.orangeAscend)));
                    btnLike.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(PostFocusActivity.this, R.color.orangeAscend)));
                    btnLike.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(PostFocusActivity.this, R.color.orangeAscend)));
                }
                else{
                    TypedValue typedValue = new TypedValue();
                    PostFocusActivity.this.getTheme().resolveAttribute(R.attr.primaryNavColor, typedValue, true);
                    int color = typedValue.data;

                    PostFocusActivity.this.getTheme().resolveAttribute(R.attr.border1, typedValue, true);
                    int border = typedValue.data;

                    btnLike.setTextColor(color);
                    btnLike.setIconTint(ColorStateList.valueOf(color));
                    btnLike.setStrokeColor(ColorStateList.valueOf(border));
                }

                btnLike.setActivated(true);
                btnLike.setAlpha(1f);

                loadingPost.setVisibility(INVISIBLE);
                post.setVisibility(View.VISIBLE);

            } else {
                Log.e("API_RESPONSE", "Resposta nula da API");
            }
        }
    }
}