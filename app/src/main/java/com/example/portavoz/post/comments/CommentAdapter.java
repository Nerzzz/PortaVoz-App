package com.example.portavoz.post.comments;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.portavoz.FormatTime;
import com.example.portavoz.R;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private List<Comment> comments = new ArrayList<>();

    public CommentAdapter() {
        // construtor vazio
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<Comment> newList) {
        comments.clear();
        comments.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comments_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView username;
        TextView content;
        TextView date;
        FormatTime formatTime = new FormatTime();

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.comment_userName);
            content = itemView.findViewById(R.id.comment_content);
            date = itemView.findViewById(R.id.comment_date);
        }

        public void bind(Comment comment) {
            username.setText(comment.user.username);
            content.setText(comment.content);
            date.setText(formatTime.formatTimeAgo(comment.createdAt));
        }
    }

}
