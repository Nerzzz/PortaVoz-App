package com.example.portavoz;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ProfilePostViewHolder extends RecyclerView.ViewHolder{
    ImageView img;
    TextView txtUpvotes, txtComments;

    String id_;

    public ProfilePostViewHolder(@NonNull View itemView) {
        super(itemView);

        img = itemView.findViewById(R.id.profilePost_img);
        txtUpvotes = itemView.findViewById(R.id.profilePost_txtUpvotes);
        txtComments = itemView.findViewById(R.id.profilePost_txtComments);
    }
}
