package com.example.portavoz.post.comments;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.portavoz.post.PostViewHolder;
import com.example.portavoz.post.vote.Vote;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;

import com.example.portavoz.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.util.ArrayList;

public class CommentViewHolder extends RecyclerView.ViewHolder {

    TextView comment_userName, comment_date, comment_content, comment_reply, comment_showMore;
    ImageView comment_userPfp;
    MaterialButton comment_btnLike;
    RecyclerView comment_responses;
    String id, parentId;
    Boolean isUpvoted;
    Vote vote = new Vote();
    int

    public CommentViewHolder(@NonNull View itemView) {
        super(itemView);

        //textViews
        comment_userName = itemView.findViewById(R.id.comment_userName);
        comment_date = itemView.findViewById(R.id.comment_date);
        comment_content = itemView.findViewById(R.id.comment_content);
        comment_reply = itemView.findViewById(R.id.comment_reply);
        comment_showMore = itemView.findViewById(R.id.comment_showMore);

        //images
        comment_userPfp = itemView.findViewById(R.id.comment_userPfp);

        comment_responses = itemView.findViewById(R.id.comment_responses);

        comment_btnLike = itemView.findViewById(R.id.comment_btnLike);
        comment_btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                mUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    @Override
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if(task.isSuccessful()){
                            if (!isUpvoted) {

                                vote.createUpvote(task.getResult().getToken(), parentId, new Vote.VoteCallback() {
                                    @Override
                                    public void onSuccess() {
                                        isUpvoted = true;
                                        //upvotes++;
                                    }

                                    @Override
                                    public void onError(String error) {
                                        Log.e("UPVOTE", error);
                                    }
                                });

                            } else {

                                vote.deleteUpvote(task.getResult().getToken(), parentId, new Vote.VoteCallback() {
                                    @Override
                                    public void onSuccess() {
                                        isUpvoted = false;
                                        //upvotes--;
                                    }

                                    @Override
                                    public void onError(String error) {
                                        Log.e("UPVOTE", error);
                                    }
                                });

                            }
                        }
                    }
                });
            }
        });

    }


}
