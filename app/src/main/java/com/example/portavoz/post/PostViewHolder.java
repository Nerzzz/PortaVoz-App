package com.example.portavoz.post;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.portavoz.R;
import com.example.portavoz.profile.PersonalProfileActivity;
import com.example.portavoz.profile.PublicProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PostViewHolder extends RecyclerView.ViewHolder {
    TextView username, created, title, desc, hashtags;
    ImageView imgUser;
    Button likes, comments;
    RecyclerView imagesCarousel;
    ImageButton btnMap;
    String userId, postId;
    String userUid = FirebaseAuth.getInstance().getUid();

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
        comments = itemView.findViewById(R.id.post_btnComment);

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

        imagesCarousel = itemView.findViewById(R.id.post_carousel);

        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(imagesCarousel);
    }
}
