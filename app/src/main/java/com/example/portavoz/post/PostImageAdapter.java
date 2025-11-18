package com.example.portavoz.post;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.portavoz.R;

import java.util.List;

public class PostImageAdapter extends RecyclerView.Adapter<PostImageViewHolder>{
    public List<String> images;

    public PostImageAdapter(List<String> images){
        this.images = images;
    }

    @NonNull
    @Override
    public PostImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PostImageViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_post_image, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull PostImageViewHolder holder, int position) {

        Glide.with(holder.itemView.getContext())
                .load(images.get(position))
                .placeholder(R.drawable.placeholder_color)
                .error(R.drawable.placeholder_color)
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }
}
