package com.example.portavoz.createPost;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.portavoz.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class imageFragment extends Fragment {

    private static final int MAX_IMAGES = 3;
    private List<Uri> selectedImages = new ArrayList<>();
    private LinearLayout uploadLayout, display_selectedImages;

    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> requestLauncher;

    private Uri cameraImageUri;

    private CreatePostViewModel viewModel;

    public imageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        if (o.getResultCode() == Activity.RESULT_OK && o.getData() != null) {
                            Intent data = o.getData();

                            if (data.getClipData() != null) {
                                int count = data.getClipData().getItemCount();
                                for (int i = 0; i < count && selectedImages.size() < MAX_IMAGES; i++) {
                                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                    selectedImages.add(imageUri);
                                }
                            } else if (data.getData() != null) {
                                Uri imageUri = data.getData();
                                if (selectedImages.size() < MAX_IMAGES) {
                                    selectedImages.add(imageUri);
                                }
                            }
                            displayImages();
                            updateViewModelImage();
                        }
                    }
                });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        if (o.getResultCode() == Activity.RESULT_OK) {
                            if (cameraImageUri != null) {
                                if (selectedImages.size() < MAX_IMAGES) {
                                    selectedImages.add(cameraImageUri);
                                    displayImages();
                                    updateViewModelImage();
                                } else {
                                    Toast.makeText(getContext(), "você atingiu o número máximo de imagens permitidas",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });

        requestLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCameraIntent();
                    } else {
                        Toast.makeText(getContext(), "Permissão de camêra é necessária para tirar fotos",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        uploadLayout = view.findViewById(R.id.uploadLayout);
        display_selectedImages = view.findViewById(R.id.display_selectedImages);

        viewModel = new ViewModelProvider(requireActivity()).get(CreatePostViewModel.class);

        uploadLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageDialog();
            }
        });

        return view;
    }

    private void openGallery() {
        if (selectedImages.size() >= MAX_IMAGES) {
            Toast.makeText(getContext(), "Você atingiu o número máximo de imagens permitidas", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        galleryLauncher.launch(Intent.createChooser(intent, "Selecione Imagens"));
    }

    private void openCamera() {
        if (selectedImages.size() >= MAX_IMAGES) {
            Toast.makeText(getContext(), "Você atingiu o número máximo de imagens permitidas", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCameraIntent();
        } else {
            requestLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCameraIntent() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePicture.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile;
            try {
                photoFile = createImageFile();
                if (!photoFile.exists()) {
                    photoFile.createNewFile();
                }
            } catch (IOException e) {
                Toast.makeText(getContext(), "Erro ao criar arquivo para foto.", Toast.LENGTH_SHORT).show();
                return;
            }

            cameraImageUri = FileProvider.getUriForFile(
                    getContext(),
                    "com.example.portavoz.fileprovider",
                    photoFile
            );

            takePicture.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            takePicture.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            takePicture.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            getActivity().grantUriPermission(
                    "com.android.camera",
                    cameraImageUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            cameraLauncher.launch(takePicture);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    private void showImageDialog() {
        String[] options = {"Galeria", "Camêra"};
        new AlertDialog.Builder(getContext())
                .setTitle("Escolher imagem de:")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openGallery();
                    } else if (which == 1) {
                        openCamera();
                    }
                }).show();
    }
    private void displayImages() {
        display_selectedImages.removeAllViews();

        for (int i = 0; i < selectedImages.size(); i++) {
            final int index = i;
            Uri uri = selectedImages.get(i);

            // Frame para imagem + botão de remover
            FrameLayout frame = new FrameLayout(getContext());
            LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(250, 250);
            frameParams.setMargins(8, 8, 8, 8);
            frame.setLayoutParams(frameParams);

            // ImageView
            ImageView imageView = new ImageView(getContext());
            FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
            imageView.setLayoutParams(imageParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(this)
                    .load(uri)
                    .into(imageView);

            // Cantos arredondados
            GradientDrawable drawable = new GradientDrawable();
            drawable.setCornerRadius(8);
            drawable.setColor(0xFFFFFFFF);
            imageView.setBackground(drawable);
            imageView.setClipToOutline(true);

            // Botão de remover
            ImageButton removeBtn = new ImageButton(getContext());
            FrameLayout.LayoutParams btnParams = new FrameLayout.LayoutParams(60, 60);
            btnParams.gravity = Gravity.TOP | Gravity.END;
            removeBtn.setLayoutParams(btnParams);
            removeBtn.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            removeBtn.setBackgroundColor(0x00000000);
            removeBtn.setOnClickListener(v -> {
                selectedImages.remove(index);
                displayImages();
                updateViewModelImage();
            });

            frame.addView(imageView);
            frame.addView(removeBtn);
            display_selectedImages.addView(frame);
        }
    }

    private void updateViewModelImage() {
        List<String> paths = new ArrayList<>();

        for (Uri uri : selectedImages) paths.add(uri.toString());
        viewModel.setImagePaths(paths);
    }


    public List<Uri> getSelectedImages() {
        return selectedImages;
    }

}