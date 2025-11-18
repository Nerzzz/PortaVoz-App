package com.example.portavoz.createPost;

import android.media.Image;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.portavoz.R;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class TagFragment extends Fragment {

    private CreatePostViewModel viewModel;
    private EditText edit_tag;
    private ImageButton add_tag_button;
    private FlexboxLayout flexbox_tags_container;
    private List<String> tags = new ArrayList<>();

    public TagFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicializa o ViewModel compartilhado pela Activity
        viewModel = new ViewModelProvider(requireActivity()).get(CreatePostViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tag, container, false);

        edit_tag = view.findViewById(R.id.edit_tag);
        add_tag_button = view.findViewById(R.id.add_tag_button);
        flexbox_tags_container = view.findViewById(R.id.flexbox_tags_container);

        // 1. Restaura o estado do ViewModel e popula a UI
        List<String> savedTags = viewModel.getHashtags().getValue();
        if (savedTags != null && !savedTags.isEmpty()) {
            tags.clear();
            tags.addAll(savedTags);
            for (String tag : tags) {
                addChipToLayout(tag);
            }
        }

        // 2. Configura o listener do botão para adicionar a tag
        add_tag_button.setOnClickListener(v -> {
            String tag = edit_tag.getText().toString().trim();
            if (!tag.isEmpty()) {
                addTag(tag);
                edit_tag.setText(""); // Limpa o campo de input
            } else {
                Toast.makeText(getContext(), "Digite uma tag válida.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void addTag(String tag) {
        // Remove o '#' se o usuário digitou
        if (tag.startsWith("#")) {
            tag = tag.substring(1);
        }

        // Verifica se a tag já existe
        if (tags.contains(tag)) {
            Toast.makeText(getContext(), "Esta tag já foi adicionada.", Toast.LENGTH_SHORT).show();
            return;
        }

        tags.add(tag);
        addChipToLayout(tag);

        viewModel.setHashtags(tags);
    }

    private void addChipToLayout(String tag) {
        Chip chip = new Chip(getContext());
        chip.setText("#" + tag);
        chip.setCloseIconVisible(true);
        chip.setClickable(true);
        chip.setCheckable(false);

        // Configura o listener para remover a tag
        chip.setOnCloseIconClickListener(v -> {
            removeTag(tag, chip);
        });

        flexbox_tags_container.addView(chip);
    }

    private void removeTag(String tag, Chip chip) {
        tags.remove(tag);
        flexbox_tags_container.removeView(chip);
        viewModel.setHashtags(tags);
    }
}