package com.example.portavoz;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.portavoz.R;

public class ImageViewHolder extends RecyclerView.ViewHolder {
    ImageView image;
    @SuppressLint("ClickableViewAccessibility")
    public ImageViewHolder(@NonNull View itemView){
        super(itemView);
        image = itemView.findViewById(R.id.post_image);

        final GestureDetector gestureDetector = new GestureDetector(itemView.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Log.v("DEBUG", "Upvoted");
                return true;
            }
        });

        image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(image.getScaleType() == ImageView.ScaleType.CENTER){
                    image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                } else {
                    image.setScaleType(ImageView.ScaleType.CENTER);
                }
                return false;
            }
        });
    }
}
