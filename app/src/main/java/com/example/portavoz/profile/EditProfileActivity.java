package com.example.portavoz.profile;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.portavoz.R;
import com.example.portavoz.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

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
                finish();
            }
        });

        btnSave = findViewById(R.id.editPf_btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

        txtUsername = findViewById(R.id.editPf_txtDisplayName);
        txtDisplayName = findViewById(R.id.editPf_txtUsername);
        txtAbout = findViewById(R.id.editPf_txtAbout);

        loadingProfile = findViewById(R.id.editPf_loadingProfile);

        pfView = findViewById(R.id.profileView);
        pfView.setVisibility(View.INVISIBLE);

        editDispayName = findViewById(R.id.editPf_eDisplayName);
        editDispayName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getUserData != null && !getUserData.isCancelled()) {
            getUserData.cancel(true);
        }
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
    public void selectType(boolean to){
        // true = banner, false = pfp
        String[] options = {"Câmera", "Galeria"};

        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
        builder.setTitle("Selecionar imagem de...");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // camera
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(to){
                    startActivityForResult(intent, 1);
                }
                else{
                    startActivityForResult(intent, 3);
                }
            } else {
                // galeria
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(to){
                    startActivityForResult(intent, 2);
                }
                else {
                    startActivityForResult(intent, 4);
                }
            }
        });
        builder.show();
    }
    private File copyUriToFile(Uri uri, String fileName) throws IOException {
        File file = new File(getCacheDir(), fileName);
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(file)) {

            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
        }
        return file;
    }
    private File saveBitmapToFile(Bitmap bitmap, String fileName) throws IOException {
        File file = new File(getCacheDir(), fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
        }
        return file;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) return;

        try {
            switch (requestCode) {
                case 1: // Câmera - banner
                    Bitmap bannerBitmap = (Bitmap) data.getExtras().get("data");
                    userBanner = saveBitmapToFile(bannerBitmap, "banner.jpg");
                    editBanner.setImageBitmap(bannerBitmap);
                    break;

                case 2: // Galeria - banner
                    Uri bannerUri = data.getData();
                    userBanner = copyUriToFile(bannerUri, "banner.jpg");
                    editBanner.setImageURI(bannerUri);
                    break;

                case 3: // Galeria - foto de perfil
                    Bitmap userBitmap = (Bitmap) data.getExtras().get("data");
                    userPfp = saveBitmapToFile(userBitmap, "pfp.jpg");
                    editUserImage.setImageBitmap(userBitmap);
                    break;

                case 4: // Câmera - foto de perfil
                    Uri userUri = data.getData();
                    userPfp = copyUriToFile(userUri, "pfp.jpg");
                    editUserImage.setImageURI(userUri);
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Falha ao processar imagem", Toast.LENGTH_SHORT).show();
        }
    }
}