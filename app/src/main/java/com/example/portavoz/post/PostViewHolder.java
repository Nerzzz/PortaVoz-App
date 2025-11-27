package com.example.portavoz.post;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.portavoz.FeedActivity;
import com.example.portavoz.R;
import com.example.portavoz.createPost.DeleteResponse;
import com.example.portavoz.createPost.PostService;
import com.example.portavoz.createPost.RetrofitClient;
import com.example.portavoz.post.comments.BottomSheetFragment;
import com.example.portavoz.post.vote.Vote;
import com.example.portavoz.profile.PersonalProfileActivity;
import com.example.portavoz.profile.PublicProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostViewHolder extends RecyclerView.ViewHolder {
    TextView username, created, title, desc, hashtags;
    ImageView imgUser;
    MaterialButton likes, comments;
    RecyclerView imagesCarousel;
    ImageButton btnMap, post_btnOptions;
    String userId, postId;
    String userUid = FirebaseAuth.getInstance().getUid();
    Vote vote = new Vote();
    boolean isUpvoted;
    int upVotesCount;

    public PostViewHolder(@NonNull View itemView){
        super(itemView);

        username = itemView.findViewById(R.id.post_txtUsername);
        created = itemView.findViewById(R.id.post_txtPostDate);

        title = itemView.findViewById(R.id.post_txtTitle);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(itemView.getContext(), PostFocusActivity.class);
                intent.putExtra("postId", postId);
                itemView.getContext().startActivity(intent);
            }
        });

        desc = itemView.findViewById(R.id.post_txtDesc);
        hashtags = itemView.findViewById(R.id.post_txtHastags);

        likes = itemView.findViewById(R.id.post_btnLike);
        likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                mUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    @Override
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if(task.isSuccessful()){
                            likes.setActivated(false);
                            likes.setAlpha(0.6f);

                            if(!isUpvoted) new UpVote(task.getResult().getToken(), postId).execute();
                            else new UnUpVote(task.getResult().getToken(), postId).execute();
                        }
                    }
                });
            }
        });

        comments = itemView.findViewById(R.id.post_btnComment);
        comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetFragment bottomSheetFragment = new BottomSheetFragment().newInstance(postId);

                androidx.fragment.app.FragmentActivity activity =
                        (androidx.fragment.app.FragmentActivity) itemView.getContext();

                bottomSheetFragment.show(activity.getSupportFragmentManager(), "comments");
            }
        });

        btnMap = itemView.findViewById(R.id.post_btnMap);

        imgUser = itemView.findViewById(R.id.post_imgUser);
        imgUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userId.equals(userUid)){
                    itemView.getContext().startActivity(new Intent(itemView.getContext(), PersonalProfileActivity.class));
                }
                else {
                    Intent intent = new Intent(itemView.getContext(), PublicProfileActivity.class);
                    intent.putExtra("userId", userId);
                    itemView.getContext().startActivity(intent);
                }
            }
        });

        post_btnOptions = itemView.findViewById(R.id.post_btnOptions);
        post_btnOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenuInflater().inflate(R.menu.dropdown_menu, popup.getMenu());

            try {
                Field[] fields = popup.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if ("mPopup".equals(field.getName())) {
                        field.setAccessible(true);
                        Object menuPopupHelper = field.get(popup);
                        Class<?> cls = Class.forName(menuPopupHelper.getClass().getName());
                        Method method = cls.getDeclaredMethod("setForceShowIcon", boolean.class);
                        method.setAccessible(true);
                        method.invoke(menuPopupHelper, true);
                        break;
                    }
                }
            } catch (Exception e) {
                // Em caso de erro
                e.printStackTrace();
            }
            if (userId.equals(userUid)) {
                popup.getMenu().findItem(R.id.action_delete).setVisible(true);
            }

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == R.id.action_report) {
                    reportPost(postId);
                }

                if (id == R.id.action_link) {
                    copyLink(postId);
                }

                if (id == R.id.action_delete) {
                    deletePost(postId);
                }
                return true;
            });

            popup.show();
        });


        imagesCarousel = itemView.findViewById(R.id.post_carousel);

        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(imagesCarousel);
    }

    public class UpVote extends AsyncTask<String, Void, String> {
        String token, postId;
        public UpVote(String token, String postId){
            this.token = token;
            this.postId = postId;
        }

        protected String doInBackground(String... strings) {
            if(isCancelled()) return null;
            HttpsURLConnection conn;
            try {
                URL url = new URL("https://portavoz.onrender.com/api/v1/posts/" + postId + "/upvote");

                conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                bw.write("{}");
                bw.flush();
                bw.close();
                os.close();

                InputStream is = conn.getResponseCode() < 400 ?
                        conn.getInputStream() : conn.getErrorStream();

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
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            Log.v("VOTE", s);

            upVotesCount++;
            isUpvoted = true;
            likes.setText(String.valueOf(upVotesCount));

            likes.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.orangeAscend)));
            likes.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.orangeAscend)));
            likes.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.orangeAscend)));

            likes.setActivated(true);
            likes.setAlpha(1f);
        }
    }


    public class UnUpVote extends AsyncTask<String, Void, String> {
        String token, postId;
        public UnUpVote(String token, String postId){
            this.token = token;
            this.postId = postId;
        }

        protected String doInBackground(String... strings) {
            if(isCancelled()) return null;
            HttpsURLConnection conn;
            try {
                URL url = new URL("https://portavoz.onrender.com/api/v1/posts/" + postId + "/desupvote");

                conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                bw.write("{}");
                bw.flush();
                bw.close();
                os.close();

                InputStream is = conn.getResponseCode() < 400 ?
                        conn.getInputStream() : conn.getErrorStream();

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
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            Log.v("VOTE", s);

            upVotesCount--;
            isUpvoted = false;
            likes.setText(String.valueOf(upVotesCount));

            TypedValue typedValue = new TypedValue();
            itemView.getContext().getTheme().resolveAttribute(R.attr.primaryNavColor, typedValue, true);
            int color = typedValue.data;

            itemView.getContext().getTheme().resolveAttribute(R.attr.border1, typedValue, true);
            int border = typedValue.data;

            likes.setTextColor(color);
            likes.setIconTint(ColorStateList.valueOf(color));
            likes.setStrokeColor(ColorStateList.valueOf(border));

            likes.setActivated(true);
            likes.setAlpha(1f);
        }
    }

    private void reportPost(String postId) {
        Toast.makeText(itemView.getContext(), "Post denunciado com sucesso", Toast.LENGTH_SHORT).show();
        //adicionar depois uma função para enviar a denúncia aos administradores
    }

    private void copyLink(String postId) {
        String url = "https://portavoz.vercel.app/post/" + postId;

        ClipboardManager clipboard  = (ClipboardManager)
                itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);

        ClipData clip = ClipData.newPlainText("Post link", url);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(itemView.getContext(), "Link copiado com sucesso!", Toast.LENGTH_SHORT).show();
    }

    private void deletePost(String postId) {
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getIdToken(true).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(itemView.getContext(), "Erro ao obter token", Toast.LENGTH_SHORT).show();
                return;
            }

            String token = task.getResult().getToken();

            PostService service = RetrofitClient.getInstance().getPostService();

            Call<DeleteResponse> call = service.deletePost(
                    "Bearer " + token,
                    postId
            );

            call.enqueue(new Callback<DeleteResponse>() {
                @Override
                public void onResponse(Call<DeleteResponse> call, Response<DeleteResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(itemView.getContext(), "Post deletado", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(itemView.getContext(), FeedActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        itemView.getContext().startActivity(intent);
                    } else {
                        Toast.makeText(itemView.getContext(), "Erro ao deletar: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<DeleteResponse> call, Throwable t) {
                    Toast.makeText(itemView.getContext(), "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

}
