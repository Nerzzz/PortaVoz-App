package com.example.portavoz;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class PublicProfileActivity extends AppCompatActivity {

    String userId;
    boolean following;
    ArrayList<Post> userPosts = new ArrayList<>();

    ImageView imgUser, imgBanner;
    TextView txtDisplayName, txtUsername, txtFollowers, txtFollowing, txtAbout, txtInfo;
    MaterialButton btnFollow;
    ProgressBar pgLoad1, loadingPosts;
    LinearLayout profileView;
    ImageButton btnReturn;
    RecyclerView rcPosts;
    ProfilePostAdapter profilePostAdapter;
    LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_public_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        final String[] token = new String[1];

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId"); // id do usuário pesquisado

        btnReturn = findViewById(R.id.publicProfile_btnReturn);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        txtDisplayName = findViewById(R.id.publicProfile_txtDisplayName);
        txtUsername = findViewById(R.id.publicProfile_txtUsername);
        txtFollowers = findViewById(R.id.publicProfile_txtFollowers);
        txtFollowing = findViewById(R.id.publicProfile_txtFollowing);
        txtAbout = findViewById(R.id.publicProfile_txtAbout);

        imgUser = findViewById(R.id.publicProfile_imgUser);
        imgBanner = findViewById(R.id.publicProfile_imgBanner);

        btnFollow = findViewById(R.id.publicProfile_btnFollow);

        pgLoad1 = findViewById(R.id.publicProfile_pgLoad1);

        profileView = findViewById(R.id.publicProfile_profileView);
        profileView.setVisibility(View.INVISIBLE);

        rcPosts = findViewById(R.id.publicProfile_rcPosts);
        linearLayoutManager = new LinearLayoutManager(this);
        rcPosts.setLayoutManager(new GridLayoutManager(this, 2));

        loadingPosts = findViewById(R.id.publicProfile_progress2);
        txtInfo = findViewById(R.id.publicProfile_txtInfo);

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    @Override
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if(task.isSuccessful()){
                            token[0] = task.getResult().getToken();

                            new GetFollowUser(token[0]).execute();
                            new GetUser(token[0]).execute();
                            new GetPosts(token[0]).execute();
                        }
                    }
                });

        btnFollow.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                btnFollow.setEnabled(false);
                btnFollow.setAlpha(0.3f);

                new GetFollowUser(token[0]).execute();
                if(following){ new UnFollowUser(token[0]).execute(); } else { new FollowUser(token[0]).execute(); }
            }
        });

    }

    // Todo: adicionar um texto indicando que não há posts
    public class GetPosts extends AsyncTask<String, Void, String>{
        String token;
        public GetPosts(String token){
            this.token = token;
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpsURLConnection conn;
            try {
                URL url = new URL("https://portavoz.onrender.com/api/v1/posts/user/"+userId);

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

        @Override
        protected void onPostExecute(String s) {
            if(s != null){
                Log.v("GetPosts", s);

                try {
                    JSONObject root = new JSONObject(s);
                    JSONArray postsArray = root.getJSONArray("posts");

                    if(postsArray.length() > 0){
                        for(int i = 0; i < postsArray.length(); i++){
                            JSONObject postObj = postsArray.getJSONObject(i);

                            String id_ = postObj.getString("_id");

                            int likes = postObj.getInt("upvotesCount");
                            int comments = postObj.getInt("commentsCount");

                            JSONArray imagesJson = postObj.getJSONArray("images");
                            List<String> images = new ArrayList<>();
                            images.add(imagesJson.getString(0));

                            userPosts.add(new Post(id_, likes, comments, images));
                        }
                    }
                    else{
                        txtInfo.setText(txtDisplayName.getText().toString()+" "+getString(R.string.noPosts));
                        txtInfo.setVisibility(View.VISIBLE);
                    }

                    profilePostAdapter = new ProfilePostAdapter(userPosts, PublicProfileActivity.this);
                    rcPosts.setAdapter(profilePostAdapter);

                    loadingPosts.setVisibility(View.INVISIBLE);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                Log.w("GetPosts", "API retornou um valor nulo");
            }
        }
    }
    public class UnFollowUser extends AsyncTask<String, Void, String>{
        String token;
        public UnFollowUser(String token){
            this.token = token;
        }
        @Override
        protected String doInBackground(String... strings) {
            HttpsURLConnection conn;

            try {
                URL url = new URL("https://portavoz.onrender.com/api/v1/users/"+userId+"/unfollow");

                conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setDoInput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                String jsonBody = "{}";
                bw.write(jsonBody);
                bw.flush();
                bw.close();
                os.close();

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
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (ProtocolException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onPostExecute(String s) {
            Log.v("UNFOLLOW",s);

            new GetFollowUser(token).execute();
            new GetUser(token).execute();
        }
    }
    public class FollowUser extends AsyncTask<String, Void, String>{
        String token;
        public FollowUser(String token){
            this.token = token;
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpsURLConnection conn;
            try {
                URL url = new URL("https://portavoz.onrender.com/api/v1/users/"+userId+"/follow");

                conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setDoInput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                String jsonBody = "{}";
                bw.write(jsonBody);
                bw.flush();
                bw.close();
                os.close();

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
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (ProtocolException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onPostExecute(String s) {
            Log.v("FOLLOW",s);

            new GetFollowUser(token).execute();
            new GetUser(token).execute();
        }
    }
    public class GetFollowUser extends AsyncTask<String, Void, String>{
        String token;
        public GetFollowUser(String token){
            this.token = token;
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpsURLConnection conn;
            URL url;
            try {
                url = new URL("https://portavoz.onrender.com/api/v1/users/"+userId+"/following");
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
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (ProtocolException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onPostExecute(String s){

            JSONObject jsonRoot;
            try {
                jsonRoot = new JSONObject(s);

                following = jsonRoot.getBoolean("isFollowing");
                Log.v("FOLLOWING", String.valueOf(following));

                if(jsonRoot.getBoolean("isFollowing")){
                    btnFollow.setText("parar de seguir");
                    btnFollow.setIcon(ContextCompat.getDrawable(PublicProfileActivity.this, R.drawable.ic_user_minus));
                }
                else{
                    btnFollow.setText("seguir");
                    btnFollow.setIcon(ContextCompat.getDrawable(PublicProfileActivity.this, R.drawable.ic_user_plus));
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public class GetUser extends AsyncTask<String, Void, String> {
        String token;
        public GetUser(String token){
            this.token = token;
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpsURLConnection conn;
            URL url;
            try {
                url = new URL("https://portavoz.onrender.com/api/v1/users/"+userId);
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
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (ProtocolException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @SuppressLint({"SetTextI18n", "ResourceAsColor"})
        @Override
        protected void onPostExecute(String s){
            Log.v("SEARCHED_USER", s);
            try {
                JSONObject jsonRoot = new JSONObject(s);
                JSONObject userObj = jsonRoot.getJSONObject("user");

                txtDisplayName.setText(userObj.getString("username"));
                txtUsername.setText(userObj.getString("fName") + " " + userObj.getString("lName"));
                txtAbout.setText(userObj.getString("about"));

                Glide.with(PublicProfileActivity.this)
                        .load(userObj.getString("image"))
                        .placeholder(R.drawable.user_image_placeholder)
                        .error(R.drawable.user_image_placeholder)
                        .into(imgUser);

                Glide.with(PublicProfileActivity.this)
                        .load(userObj.getString("banner"))
                        .placeholder(R.drawable.user_image_placeholder)
                        .error(R.drawable.user_image_placeholder)
                        .into(imgBanner);

                JSONObject meta = userObj.getJSONObject("meta");
                JSONObject counters = meta.getJSONObject("counters");
                txtFollowing.setText("Seguindo: " + counters.getString("following"));
                txtFollowers.setText("Seguidores: " + counters.getString("followers"));

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            pgLoad1.setVisibility(View.INVISIBLE);
            profileView.setVisibility(View.VISIBLE);

            loadingPosts.setVisibility(View.VISIBLE);

            btnFollow.setEnabled(true);
            btnFollow.setAlpha(1f);
        }
    }
}