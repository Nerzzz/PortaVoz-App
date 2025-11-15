package com.example.portavoz.profile;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.portavoz.R;
import com.example.portavoz.User;
import com.example.portavoz.network.ApiService;
import com.example.portavoz.network.RetrofitClient;
import com.example.portavoz.network.UserResponse;
import com.example.portavoz.network.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class EditProfileActivity extends AppCompatActivity {
    Button btnReturn, btnSave;
    LinearLayout editDispayName, editUsername, editAbout, pfView;
    TextView txtDisplayName, txtUsername, txtAbout;
    ImageView editBanner, editUserImage;
    ProgressBar loadingProfile;

    User user;
    FirebaseUser mUser;
    File userPfp, userBanner;

    GetUserData getUserData;

    boolean pfpModified = false;
    boolean bannerModified = false;

    ApiService apiService;

    private Uri cameraOutputUriBanner;
    private Uri cameraOutputUriPfp;

    private File outputFileBanner;
    private File outputFilePfp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.profile_activity_edit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnReturn = findViewById(R.id.editPf_btnReturn);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = LayoutInflater.from(EditProfileActivity.this).inflate(R.layout.alert_confirmation, null);

                TextView title = dialogView.findViewById(R.id.confirmation_txtTitle);
                title.setText("Sair");

                TextView desc = dialogView.findViewById(R.id.confirmation_txtDesc);
                desc.setText("Tem certeza que deseja sair? As alterações não serão mantidas.");

                Button confirm = dialogView.findViewById(R.id.confirmation_btnConfirm);
                Button cancel = dialogView.findViewById(R.id.confirmation_btnCancel);

                confirm.setText("Sair");
                cancel.setText("Não");

                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        finish();
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        btnSave = findViewById(R.id.editPf_btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = LayoutInflater.from(EditProfileActivity.this).inflate(R.layout.alert_confirmation, null);

                TextView title = dialogView.findViewById(R.id.confirmation_txtTitle);
                title.setText("Salvar mudanças");

                TextView desc = dialogView.findViewById(R.id.confirmation_txtDesc);
                desc.setText("Deseja salvar as alterações no perfil? Não será possível desfaze-las.");

                Button confirm = dialogView.findViewById(R.id.confirmation_btnConfirm);
                Button cancel = dialogView.findViewById(R.id.confirmation_btnCancel);

                confirm.setText("Salvar");
                cancel.setText("Não");

                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mUser = FirebaseAuth.getInstance().getCurrentUser();
                        mUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                            @Override
                            public void onComplete(@NonNull Task<GetTokenResult> task) {
                                if(task.isSuccessful()){
                                    try {
                                        updateUserData(task.getResult().getToken(),
                                                mUser.getUid(),
                                                user.username,
                                                user.fName,
                                                user.lName,
                                                user.about,
                                                userPfp,
                                                userBanner);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        });
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        editBanner = findViewById(R.id.editPf_imgBanner);
        editBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectType(true);
            }
        });

        editUserImage = findViewById(R.id.editPf_imgUser);
        editUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectType(false);
            }
        });

        txtDisplayName = findViewById(R.id.editPf_txtDisplayName);
        txtUsername = findViewById(R.id.editPf_txtUsername);
        txtAbout = findViewById(R.id.editPf_txtAbout);

        loadingProfile = findViewById(R.id.editPf_loadingProfile);

        pfView = findViewById(R.id.profileView);
        pfView.setVisibility(View.INVISIBLE);

        editDispayName = findViewById(R.id.editPf_eDisplayName);
        editDispayName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = LayoutInflater.from(EditProfileActivity.this).inflate(R.layout.edit_input_alert, null);

                EditText value1 = dialogView.findViewById(R.id.edit_etValue1);
                value1.setText(user.username);
                value1.setHint("Nome de exibição");

                EditText value2 = dialogView.findViewById(R.id.edit_etValue2);
                value2.setVisibility(View.GONE);

                Button btnOk = dialogView.findViewById(R.id.edit_btnChange);
                Button btnNo = dialogView.findViewById(R.id.edit_btnCancel);

                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(value1.getText().toString().isEmpty()){
                            Toast.makeText(EditProfileActivity.this, "O campo não deve estar vazio!", Toast.LENGTH_SHORT).show();
                        }
                        else if(!value1.getText().toString().matches("^([A-Za-z_.-])*$")){
                            Toast.makeText(EditProfileActivity.this, "Utilize caracteres válidos!", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            user.username = value1.getText().toString();
                            reloadUserData();
                            dialog.dismiss();
                        }
                    }
                });

                btnNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        editUsername = findViewById(R.id.editPf_eUsername);
        editUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("CLICK", "Fui crikado1");

                View dialogView = LayoutInflater.from(EditProfileActivity.this).inflate(R.layout.edit_input_alert, null);

                EditText value1 = dialogView.findViewById(R.id.edit_etValue1);
                value1.setText(user.fName);
                value1.setHint("Nome");

                EditText value2 = dialogView.findViewById(R.id.edit_etValue2);
                value2.setText(user.lName);
                value2.setHint("Sobrenome");

                Button btnOk = dialogView.findViewById(R.id.edit_btnChange);
                Button btnNo = dialogView.findViewById(R.id.edit_btnCancel);

                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(value1.getText().toString().isEmpty() || value2.getText().toString().isEmpty()){
                            Toast.makeText(EditProfileActivity.this, "Os campos não devem estar vazios!", Toast.LENGTH_SHORT).show();
                        }
                        else if(!value1.getText().toString().matches("^([A-Za-z])*$") || !value2.getText().toString().matches("^([A-Za-z])*$")){
                            Toast.makeText(EditProfileActivity.this, "Utilize caracteres válidos!", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            user.fName = value1.getText().toString();
                            user.lName = value2.getText().toString();
                            reloadUserData();
                            dialog.dismiss();
                        }
                    }
                });

                btnNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        editAbout = findViewById(R.id.editPf_eAbout);
        editAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = LayoutInflater.from(EditProfileActivity.this).inflate(R.layout.edit_input_alert, null);

                EditText value1 = dialogView.findViewById(R.id.edit_etValue1);
                value1.setText(user.about);
                value1.setHint("Sobre mim");

                EditText value2 = dialogView.findViewById(R.id.edit_etValue2);
                value2.setVisibility(View.GONE);

                Button btnOk = dialogView.findViewById(R.id.edit_btnChange);
                Button btnNo = dialogView.findViewById(R.id.edit_btnCancel);

                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        user.about = value1.getText().toString();
                        reloadUserData();
                        dialog.dismiss();
                    }
                });

                btnNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if(task.isSuccessful()){
                    getUserData = new GetUserData(task.getResult().getToken(), mUser.getUid());
                    getUserData.execute();
                }
            }
        });
    }

    // TODO: Arrumar erro de não atualizar se o usuário mandar QUALQUER imagem
    public void updateUserData(String token, String id, String username, String fName, String lName, String about, File pfp, File banner) throws IOException {
        Retrofit retrofit = RetrofitClient.getClient("https://portavoz.onrender.com/", token);
        apiService = retrofit.create(ApiService.class);

        Map<String, RequestBody> map = new HashMap<>();

        if(username != null && !username.isEmpty()) map.put("username", Utils.createTextPart(username));
        if(fName != null && !fName.isEmpty())   map.put("fName", Utils.createTextPart(fName));
        if(lName != null && !lName.isEmpty())   map.put("lName", Utils.createTextPart(lName));
        if(about != null && !about.isEmpty())   map.put("about", Utils.createTextPart(about));

        MultipartBody.Part imagePart = Utils.createFilePart("image", userPfp);
        MultipartBody.Part bannerPart = Utils.createFilePart("banner", userBanner);

        Call<UserResponse> call = apiService.updateUser(id, map, imagePart, bannerPart);

        // Call<UserResponse> call = apiService.updateUser(
        //        id,
        //        Utils.createTextPart(username),
        //        Utils.createTextPart(fName),
        //        Utils.createTextPart(lName),
        //        Utils.createTextPart(about),
        //        imagePart,
        //        bannerPart
        //);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if(response.isSuccessful()){
                    UserResponse userResponse = response.body();
                    Toast.makeText(EditProfileActivity.this, "Deu certo!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(EditProfileActivity.this, "Falha: " + response.code(), Toast.LENGTH_SHORT).show();
                    try {
                        Log.e("API_ERROR", response.errorBody().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable throwable) {
                Toast.makeText(EditProfileActivity.this, "Erro: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    private File createImageFile(String name) throws IOException {
        File file = new File(getCacheDir(), name + "_" + System.currentTimeMillis() + ".jpg");
        file.createNewFile();
        return file;
    }
    private void openCamera(boolean banner) {
        try {
            File file = createImageFile(banner ? "banner" : "pfp");

            if (banner) outputFileBanner = file;
            else outputFilePfp = file;

            Uri uri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    file
            );

            if (banner) cameraOutputUriBanner = uri;
            else cameraOutputUriPfp = uri;

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(intent, banner ? 1 : 3);

        } catch (IOException e) {
            Toast.makeText(this, "Falhou ao criar arquivo", Toast.LENGTH_SHORT).show();
        }
    }
    private void openGallery(boolean banner) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, banner ? 2 : 4);
    }
    private File copyUriToCache(Uri uri, String prefix) throws IOException {
        InputStream in = getContentResolver().openInputStream(uri);
        File file = createImageFile(prefix);
        OutputStream out = new FileOutputStream(file);

        byte[] buffer = new byte[4096];
        int len;

        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }

        in.close();
        out.close();

        return file;
    }


    public void reloadUserData(){
        txtDisplayName.setText(user.username);
        txtUsername.setText(user.fName + " " + user.lName);
        txtAbout.setText(user.about);
    }
    public class GetUserData extends AsyncTask<String, Void, String>{
        String token, uid;

        public GetUserData(String token, String uid){
            this.token = token;
            this.uid = uid;
        }

        @Override
        protected String doInBackground(String... strings) {
            if(isCancelled()) return null;

            HttpsURLConnection conn;
            try {
                URL url = new URL("https://portavoz.onrender.com/api/v1/users/"+uid);

                conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setDoInput(true);

                InputStream is = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                br.close();
                is.close();
                conn.disconnect();

                return sb.toString();

            } catch (ProtocolException e) {
                throw new RuntimeException(e);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                Log.e("API_ERROR_USER", "Erro ao acessar API", e);
                return null;
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String APIResult_user){
            if(APIResult_user != null || isCancelled()){
                try {
                    Log.v("CURRENT_USER_DATA", APIResult_user);

                    JSONObject root = new JSONObject(APIResult_user);

                    JSONObject userObj = root.getJSONObject("user");

                    user = new User(
                            userObj.getString("username"),
                            userObj.getString("fName"),
                            userObj.getString("lName"),
                            userObj.getString("about"),
                            userObj.getString("image"),
                            userObj.getString("banner"));

                    Glide.with(EditProfileActivity.this)
                            .load(user.banner)
                            .placeholder(R.color.placeholderColor)
                            .error(R.color.placeholderColor)
                            .into(editBanner);

                    Glide.with(EditProfileActivity.this)
                            .load(user.image)
                            .placeholder(R.drawable.user_image_placeholder)
                            .error(R.drawable.user_image_placeholder)
                            .into(editUserImage);

                    txtDisplayName.setText(user.username);
                    txtUsername.setText(user.fName + " " + user.lName);
                    txtAbout.setText(user.about);

                    pfView.setVisibility(View.VISIBLE);
                    loadingProfile.setVisibility(View.INVISIBLE);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    public void selectType(boolean banner) {
        String[] options = {"Câmera", "Galeria"};

        new AlertDialog.Builder(this)
                .setTitle("Selecionar imagem")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openCamera(banner);
                    else openGallery(banner);
                })
                .show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        try {
            switch (requestCode) {

                // Câmera banner
                case 1:
                    userBanner = outputFileBanner;
                    editBanner.setImageURI(cameraOutputUriBanner);
                    bannerModified = true;
                    break;

                // Galeria banner
                case 2:
                    Uri bannerUri = data.getData();
                    userBanner = copyUriToCache(bannerUri, "banner");
                    editBanner.setImageURI(bannerUri);
                    bannerModified = true;
                    break;

                // Câmera pfp
                case 3:
                    userPfp = outputFilePfp;
                    editUserImage.setImageURI(cameraOutputUriPfp);
                    pfpModified = true;
                    break;

                // Galeria pfp
                case 4:
                    Uri uri = data.getData();
                    userPfp = copyUriToCache(uri, "pfp");
                    editUserImage.setImageURI(uri);
                    pfpModified = true;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao processar imagem", Toast.LENGTH_SHORT).show();
        }
    }
}