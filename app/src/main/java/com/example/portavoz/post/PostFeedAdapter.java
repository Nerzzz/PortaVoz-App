package com.example.portavoz.post;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.portavoz.R;

import java.util.ArrayList;


public class PostFeedAdapter extends RecyclerView.Adapter<PostViewHolder> {
    ArrayList<Post> posts;

    public PostFeedAdapter(ArrayList<Post> p, Context ctx){
        posts = p;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PostViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_post, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        holder.username.setText(posts.get(position).username);
        holder.created.setText(posts.get(position).created);
        holder.likes.setText(String.valueOf(posts.get(position).likes));
        holder.comments.setText(String.valueOf(posts.get(position).comments));
        holder.title.setText(posts.get(position).title);
        holder.desc.setText(posts.get(position).desc);

        holder.userId = posts.get(position).userId;
        holder.postId = posts.get(position).id_;

        Glide.with(holder.itemView.getContext())
                .load(posts.get(position).userImage)
                .placeholder(R.drawable.user_image_placeholder)
                .error(R.drawable.user_image_placeholder)
                .into(holder.imgUser);

        holder.hashtags.setText("#" + String.join(" #", posts.get(position).hashtags));

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(holder.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false);
        holder.imagesCarousel.setLayoutManager(layoutManager);
        holder.imagesCarousel.setAdapter(new PostImageAdapter(posts.get(position).images));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}
