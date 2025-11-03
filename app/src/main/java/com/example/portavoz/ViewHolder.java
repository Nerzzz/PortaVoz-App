package com.example.portavoz;

import android.content.Intent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.portavoz.R;

public class ViewHolder extends RecyclerView.ViewHolder {
    TextView username, created, title, desc, hashtags;
    ImageView imgUser;
    Button likes, comments;
    RecyclerView imagesCarousel;
    String userId, postId;

    public ViewHolder(@NonNull View itemView){
        super(itemView);

        username = itemView.findViewById(R.id.post_txtUsername);
        created = itemView.findViewById(R.id.post_txtPostDate);
        title = itemView.findViewById(R.id.post_txtTitle);
        desc = itemView.findViewById(R.id.post_txtDesc);
        hashtags = itemView.findViewById(R.id.post_txtHastags);

        likes = itemView.findViewById(R.id.post_btnLike);
        comments = itemView.findViewById(R.id.post_btnComment);

        imgUser = itemView.findViewById(R.id.post_imgUser);
        imgUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(itemView.getContext(), PublicProfileActivity.class);
                intent.putExtra("userId", userId);
                itemView.getContext().startActivity(intent);
            }
        });

        imagesCarousel = itemView.findViewById(R.id.post_carousel);

        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(imagesCarousel);
    }
}
