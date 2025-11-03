package com.example.portavoz;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ProfilePostAdapter extends RecyclerView.Adapter<ProfilePostViewHolder> {
    ArrayList<Post> posts;

    public ProfilePostAdapter(ArrayList<Post> posts, Context ctx){
        this.posts = posts;
    }

    @NonNull
    @Override
    public ProfilePostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProfilePostViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_profile_post, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ProfilePostViewHolder holder, int position) {
        holder.txtUpvotes.setText(posts.get(position).likes);
        holder.txtComments.setText(posts.get(position).comments);

        Glide.with(holder.itemView.getContext())
                .load(posts.get(position).images.get(0))
                .placeholder(R.drawable.user_image_placeholder)
                .error(R.drawable.user_image_placeholder)
                .into(holder.img);

        holder.id_ = posts.get(position).id_;
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}
