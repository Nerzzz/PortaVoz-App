package com.example.portavoz.createPost;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.portavoz.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreatePost extends AppCompatActivity {

    int curFragment = 1;
    Button btnNext;
    ImageButton btnPrevious;
    CreatePostViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_post);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(CreatePostViewModel.class);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new ContentFragment())
                .commit();

        btnNext.setOnClickListener(v -> {
            if (curFragment < 4) {
                curFragment++;
                showFragment(curFragment);

            } else {
                btnNext.setEnabled(false);
                btnNext.setAlpha(0.6f);
                enviarParaApi();
            }
        });

        btnPrevious.setOnClickListener(v -> {
            if (curFragment > 1) {
                curFragment--;
                showFragment(curFragment);
            } else {
                finish();
            }
        });
    }

    private void showFragment(int n) {
        Fragment fragment = null;
        switch (n) {
            case 1: fragment = new ContentFragment(); break;
            case 2: fragment = new imageFragment(); break;
            case 3: fragment = new TagFragment(); break;
            case 4: fragment = new MapFragment(); break;
        }

        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        }

        btnNext.setText(n == 4 ? "Finalizar" : "Próximo");
    }

    private File compressImage(Context context, Uri uri) throws IOException {
        InputStream input = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input);

        File compressedFile = new File(context.getCacheDir(), "compressed_" + System.currentTimeMillis() + ".jpg");
        FileOutputStream output = new FileOutputStream(compressedFile);

        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, output);

        output.flush();
        output.close();
        input.close();

        return compressedFile;
    }

    private void validate(String stage, Runnable onSuccess) {
        MediaType textPlain = MediaType.parse("text/plain");
        RequestBody rbTitle = RequestBody.create(viewModel.getTitle().getValue(), textPlain);
        RequestBody rbDesc = RequestBody.create(viewModel.getDescription().getValue(), textPlain);

        List<MultipartBody.Part> imageParts = new ArrayList<>();
        if (stage.equals("images") && viewModel.getImagePaths().getValue() != null) {
            for (String uri : viewModel.getImagePaths().getValue()) {
                try {
                    File file = compressImage(this, Uri.parse(uri));
                    RequestBody reqFile = RequestBody.create(file, MediaType.parse("image/*"));
                    imageParts.add(MultipartBody.Part.createFormData("images", file.getName(), reqFile));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        List<MultipartBody.Part> hashtagParts = new ArrayList<>();
        if (stage.equals("hashtags") && viewModel.getHashtags().getValue() != null) {
            for (String tag : viewModel.getHashtags().getValue()) {
                hashtagParts.add(MultipartBody.Part.createFormData("hashtags", tag));
            }
        }

        RetrofitClient.getInstance()
                .getValidationService()
                .validateStage(stage, rbTitle, rbDesc, imageParts, hashtagParts)
                .enqueue(new Callback<ValidationResponse>() {
                    @Override
                    public void onResponse(Call<ValidationResponse> call, Response<ValidationResponse> response) {

                        Log.e("VALIDATE_DEBUG", "Status code: " + response.code());

                        if (response.isSuccessful()) {
                            ValidationResponse res = response.body();
                            Log.e("VALIDATE_DEBUG", "Corpo da resposta: " + new Gson().toJson(res));
                        } else {
                            try {
                                Log.e("VALIDATE_DEBUG", "Erro da API: " + response.errorBody().string());
                            } catch (Exception e) {
                                Log.e("VALIDATE_DEBUG", "Erro lendo o corpo de erro", e);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ValidationResponse> call, Throwable t) {
                        Log.e("VALIDATE_DEBUG", "Falha da requisição", t);
                    }
                });

    }

    private void enviarParaApi() {
        String title = viewModel.getTitle().getValue();
        String desc = viewModel.getDescription().getValue();
        Double lat = viewModel.getLatitude().getValue();
        Double lon = viewModel.getLongitude().getValue();
        String address = viewModel.getAddress().getValue();
        List<String> hashtags = viewModel.getHashtags().getValue();
        List<String> imageUris = viewModel.getImagePaths().getValue();

        // VALIDAÇÕES OBRIGATÓRIAS
        if (title == null || title.isEmpty()) {
            Toast.makeText(this, "Preencha o título!", Toast.LENGTH_LONG).show();
            btnNext.setEnabled(true);
            btnNext.setAlpha(1f);
            return;
        }

        if (desc == null || desc.isEmpty()) {
            Toast.makeText(this, "Preencha a descrição!", Toast.LENGTH_LONG).show();
            btnNext.setEnabled(true);
            btnNext.setAlpha(1f);
            return;
        }

        if (lat == null || lon == null) {
            Toast.makeText(this, "Selecione uma localização!", Toast.LENGTH_LONG).show();
            btnNext.setEnabled(true);
            btnNext.setAlpha(1f);
            return;
        }

        if (address == null || address.isEmpty()) {
            Toast.makeText(this, "Preencha o endereço!", Toast.LENGTH_LONG).show();
            btnNext.setEnabled(true);
            btnNext.setAlpha(1f);
            return;
        }

        // VALIDAÇÃO DE HASHTAGS (OBRIGATÓRIO)
        if (hashtags == null || hashtags.isEmpty()) {
            Toast.makeText(this, "Adicione pelo menos uma hashtag!", Toast.LENGTH_LONG).show();
            btnNext.setEnabled(true);
            btnNext.setAlpha(1f);
            return;
        }

        // VALIDAÇÃO DE COORDENADAS
        if (lat < -90 || lat > 90) {
            Toast.makeText(this, "Latitude inválida! Deve estar entre -90 e 90.", Toast.LENGTH_LONG).show();
            btnNext.setEnabled(true);
            btnNext.setAlpha(1f);
            return;
        }

        if (lon < -180 || lon > 180) {
            Toast.makeText(this, "Longitude inválida! Deve estar entre -180 e 180.", Toast.LENGTH_LONG).show();
            btnNext.setEnabled(true);
            btnNext.setAlpha(1f);
            return;
        }

        // VALIDAÇÃO DE LIMITE DE IMAGENS
        if (imageUris != null && imageUris.size() > 3) {
            Toast.makeText(this, "Máximo de 3 imagens permitidas!", Toast.LENGTH_LONG).show();
            btnNext.setEnabled(true);
            btnNext.setAlpha(1f);
            return;
        }

        FirebaseAuth.getInstance().getCurrentUser().getIdToken(true)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "Falha ao obter token", Toast.LENGTH_LONG).show();
                        btnNext.setEnabled(true);
                        btnNext.setAlpha(1f);
                        return;
                    }

                    String token = "Bearer " + task.getResult().getToken();

                    // CAMPOS DE TEXTO SIMPLES
                    MediaType textPlain = MediaType.parse("text/plain");
                    RequestBody rbTitle = RequestBody.create(title, textPlain);
                    RequestBody rbDesc = RequestBody.create(desc, textPlain);
                    RequestBody rbAddress = RequestBody.create(address, textPlain);
                    RequestBody rbStatus = RequestBody.create("ativo", textPlain);

                    // LOCATION
                    RequestBody rbLatitude = RequestBody.create(String.valueOf(lat), textPlain);
                    RequestBody rbLongitude = RequestBody.create(String.valueOf(lon), textPlain);

                    // HASHTAGS
                    List<MultipartBody.Part> hashtagParts = new ArrayList<>();
                    for (String tag : hashtags) {
                        hashtagParts.add(MultipartBody.Part.createFormData("hashtags", tag));
                    }

                    // IMAGENS
                    List<MultipartBody.Part> imageParts = new ArrayList<>();

                    if (imageUris != null && !imageUris.isEmpty()) {
                        for (String uri : imageUris) {
                            try {
                                Uri imageUri = Uri.parse(uri);
                                File file = compressImage(this, imageUri);
                                String mimeType = getContentResolver().getType(imageUri);
                                if (mimeType == null) mimeType = "image/jpeg";

                                RequestBody reqFile = RequestBody.create(file, MediaType.parse(mimeType));

                                MultipartBody.Part part = MultipartBody.Part.createFormData(
                                        "images",
                                        file.getName(),
                                        reqFile
                                );

                                imageParts.add(part);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Erro ao processar imagem!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    // CHAMADA RETROFIT
                    RetrofitClient.getInstance()
                            .getPostService()
                            .createPost(
                                    token,
                                    rbTitle,
                                    rbDesc,
                                    hashtagParts,
                                    rbLatitude,
                                    rbLongitude,
                                    rbAddress,
                                    rbStatus,
                                    imageParts
                            )
                            .enqueue(new Callback<PostResponse>() {
                                @Override
                                public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(CreatePost.this, "Post criado com sucesso!", Toast.LENGTH_LONG).show();
                                        finish();
                                    } else {
                                        try {
                                            String error = response.errorBody().string();
                                            Log.e("API_ERROR", "Código: " + response.code() + " | Erro: " + error);
                                            if (response.code() == 500) {
                                                Toast.makeText(CreatePost.this, "Conteúdo não relacionado a infraestrutura. | Hashtags fora de contexto ou inapropriadas. | Imagem fora de contexto da denúncia",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Toast.makeText(CreatePost.this, "Erro desconhecido", Toast.LENGTH_LONG).show();
                                        }
                                        btnNext.setEnabled(true);
                                        btnNext.setAlpha(1f);
                                    }
                                }

                                @Override
                                public void onFailure(Call<PostResponse> call, Throwable t) {
                                    Log.e("API_ERROR", "Falha na requisição", t);
                                    Toast.makeText(CreatePost.this, "Falha: " + t.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                });
    }
}
