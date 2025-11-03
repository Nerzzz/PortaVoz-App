package com.example.portavoz;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.portavoz.R;

import java.util.List;

public class PostImageAdapter extends RecyclerView.Adapter<ImageViewHolder>{
    public List<String> images;

    public PostImageAdapter(List<String> images){
        this.images = images;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ImageViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_post_image, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {

        Glide.with(holder.itemView.getContext())
                .load(images.get(position))
                .placeholder(R.drawable.avatar)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }
}
