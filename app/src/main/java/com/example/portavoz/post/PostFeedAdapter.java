package com.example.portavoz.post;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.portavoz.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
    public void onBindViewHolder(@NonNull PostViewHolder holder, @SuppressLint("RecyclerView") int position) {
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

        holder.btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context ctx = v.getContext();
                View dialogView = LayoutInflater.from(ctx).inflate(R.layout.post_layout_map, null);

                TextView txtAdress = dialogView.findViewById(R.id.map_txtAdress);
                MapView mapView = dialogView.findViewById(R.id.map_mapView);

                txtAdress.setText(posts.get(position).address);

                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                dialog.show();

                mapView.onCreate(null);
                mapView.getMapAsync(googleMap -> {
                    LatLng koordinaty = new LatLng(posts.get(position).lat, posts.get(position).lon);

                    googleMap.addMarker(new MarkerOptions()
                            .position(koordinaty)
                            .title("Localização")
                            .snippet(posts.get(position).address));

                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(koordinaty, 15f));
                });

                mapView.onResume();
            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}
