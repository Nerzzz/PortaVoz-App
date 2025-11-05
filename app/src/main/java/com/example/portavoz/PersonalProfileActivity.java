package com.example.portavoz;

import android.annotation.SuppressLint;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
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
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class PersonalProfileActivity extends AppCompatActivity {

    ImageView imgUser, imgBanner;
    TextView txtDisplayName, txtUsername, txtFollowers, txtFollowing, txtAbout;
    RecyclerView rcPosts;
    ProfilePostAdapter profilePostAdapter;
    LinearLayoutManager linearLayoutManager;
    ProgressBar loadingUserData;
    LinearLayout profileView;
    ImageButton btnReturn;

    FirebaseUser user;
    ArrayList<Post> userPosts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imgUser = findViewById(R.id.personalProfile_imgUser);
        imgBanner = findViewById(R.id.personalProfile_imgBanner);

        txtDisplayName = findViewById(R.id.personalProfile_txtDisplayName);
        txtUsername = findViewById(R.id.personalProfile_txtUsername);
        txtDisplayName = findViewById(R.id.personalProfile_txtDisplayName);

        txtFollowers = findViewById(R.id.personalProfile_txtFollowers);
        txtFollowing = findViewById(R.id.personalProfile_txtFollowing);

        txtAbout = findViewById(R.id.personalProfile_txtAbout);

        rcPosts = findViewById(R.id.personalProfile_rcPosts);
        linearLayoutManager = new LinearLayoutManager(this);
        rcPosts.setLayoutManager(new GridLayoutManager(this, 2));

        loadingUserData = findViewById(R.id.personalProfile_progress1);
        loadingUserData.setVisibility(TextView.VISIBLE);

        profileView = findViewById(R.id.personalProfile_profileView);
        profileView.setVisibility(TextView.INVISIBLE);

        btnReturn = findViewById(R.id.personalProfile_btnReturn);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        user = FirebaseAuth.getInstance().getCurrentUser();
        user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if(task.isSuccessful()){
                    new UserMainData(task.getResult().getToken(), user.getUid()).execute();
                    new UserPosts(task.getResult().getToken(), user.getUid()).execute();
                }
            }
        });
    }

    public class UserMainData extends AsyncTask<String, Void, String>{
        String token, userId;
        public UserMainData(String token, String userId){
            this.token = token;
            this.userId = userId;
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpsURLConnection conn;
            try {
                FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                String userId = mUser.getUid();

                URL url = new URL("https://portavoz.onrender.com/api/v1/users/"+userId);

                conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Authorization", "Bearer " + token);
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

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String APIResult_user){
            if(APIResult_user != null){
                try {
                    Log.v("CURRENT_USER_DATA", APIResult_user);

                    JSONObject root = new JSONObject(APIResult_user);

                    JSONObject userObj = root.getJSONObject("user");

                    txtDisplayName.setText(userObj.getString("username"));
                    txtUsername.setText(userObj.getString("fName")+" "+userObj.getString("lName"));
                    txtAbout.setText(userObj.getString("about"));

                    if(!userObj.getString("image").isEmpty()){
                        Glide.with(PersonalProfileActivity.this)
                                .load(userObj.getString("banner"))
                                .placeholder(R.color.navColor1)
                                .error(R.color.navColor1)
                                .into(imgBanner);
                    }
                    else{
                        imgBanner.setImageResource(R.color.navColor1);
                    }

                    Glide.with(PersonalProfileActivity.this)
                            .load(userObj.getString("image"))
                            .placeholder(R.drawable.user_image_placeholder)
                            .error(R.drawable.user_image_placeholder)
                            .into(imgUser);

                    JSONObject meta = userObj.getJSONObject("meta");
                    JSONObject counters = meta.getJSONObject("counters");
                    txtFollowers.setText("Seguidores: " + counters.getInt("followers"));
                    txtFollowing.setText("Seguindo: "+ counters.getInt("following"));

                    profileView.setVisibility(View.VISIBLE);
                    loadingUserData.setVisibility(View.INVISIBLE);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    // Todo: adicionar um texto indicando que não há posts
    public class UserPosts extends  AsyncTask<String, Void, String>{
        String token, userId;
        public UserPosts(String token, String userId){
            this.token = token;
            this.userId = userId;
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

                    profilePostAdapter = new ProfilePostAdapter(userPosts, PersonalProfileActivity.this);
                    rcPosts.setAdapter(profilePostAdapter);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                Log.w("GetPosts", "API retornou um valor nulo");
            }
        }
    }
}