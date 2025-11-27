package com.example.portavoz.post.comments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.portavoz.R;
import com.example.portavoz.createPost.RetrofitClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BottomSheetFragment extends BottomSheetDialogFragment {

    private String postId;
    private RecyclerView recyclerView;
    private CommentAdapter adapter;

    public static BottomSheetFragment newInstance(String postId) {
        BottomSheetFragment fragment = new BottomSheetFragment();
        Bundle args = new Bundle();
        args.putString("postId", postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            postId = getArguments().getString("postId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.layout_bottom_sheet, container, false);

        recyclerView = view.findViewById(R.id.comments_display);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new CommentAdapter();
        recyclerView.setAdapter(adapter);

        carregarTokenEComentarios();

        return view;
    }

    private void carregarTokenEComentarios() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Log.e("COMMENTS", "Usuário não autenticado.");
            return;
        }

        user.getIdToken(true).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("TOKEN_ERROR", "Erro ao pegar token", task.getException());
                return;
            }

            String token = task.getResult().getToken();
            carregarComentarios(token);
        });
    }

    private void carregarComentarios(String token) {

        CommentService service = RetrofitClient.getInstance().getCommentService();

        service.getComments("Bearer " + token, postId, 1)
                .enqueue(new Callback<CommentResponse>() {
                    @Override
                    public void onResponse(Call<CommentResponse> call, Response<CommentResponse> response) {

                        if (!response.isSuccessful()) {
                            Log.e("COMMENTS", "Erro HTTP: " + response.code());
                            return;
                        }

                        CommentResponse resp = response.body();

                        if (resp == null || resp.getComments() == null) {
                            Log.e("COMMENTS", "Resposta vazia da API.");
                            return;
                        }

                        adapter.updateList(resp.getComments());
                    }

                    @Override
                    public void onFailure(Call<CommentResponse> call, Throwable t) {
                        Log.e("COMMENTS", "Falha na requisição: " + t.getMessage());
                    }
                });
    }
}
