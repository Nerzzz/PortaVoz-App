package com.example.portavoz.createPost;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.example.portavoz.R;

public class ContentFragment extends Fragment {

    private EditText edt_title, edt_content;
    private CreatePostViewModel viewModel;

    public ContentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(CreatePostViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_content, container, false);

        edt_title = view.findViewById(R.id.edt_title);
        edt_content = view.findViewById(R.id.edt_content);

        edt_title.setText(viewModel.getTitle().getValue() != null ? viewModel.getTitle().getValue() : "");
        edt_content.setText(viewModel.getDescription().getValue() != null ? viewModel.getDescription().getValue() : "");

        edt_title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setTitle(s.toString());
            }
        });

        edt_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setDescription(s.toString());
            }
        });

        return view;
    }
}
