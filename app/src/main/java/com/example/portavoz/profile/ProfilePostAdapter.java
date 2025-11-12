package com.example.portavoz.profile;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.portavoz.post.Post;
import com.example.portavoz.R;
import com.example.portavoz.post.PostFocusActivity;

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
                        .inflate(R.layout.profile_layout_post, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ProfilePostViewHolder holder, int position) {
        holder.txtUpvotes.setText(String.valueOf(posts.get(position).likes));
        holder.txtComments.setText(String.valueOf(posts.get(position).comments));

        Glide.with(holder.itemView.getContext())
                .load(posts.get(position).images.get(0))
                .placeholder(R.drawable.user_image_placeholder)
                .error(R.drawable.user_image_placeholder)
                .into(holder.img);

        holder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), PostFocusActivity.class);
                intent.putExtra("postId", posts.get(position).id_);
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}
