package com.example.portavoz.auth;

import android.content.Intent;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.portavoz.FeedActivity;
import com.example.portavoz.MainActivity;
import com.example.portavoz.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class RegisterActivity extends AppCompatActivity {

    ImageButton btnSeePsw;
    EditText etFName, etLName, etLogin, etPsw;
    Button btnRegister, btnReturn;
    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.auth_activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnReturn = findViewById(R.id.register_btnReturn);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish();
            }
        });

        etFName = findViewById(R.id.register_etFName);
        etLName = findViewById(R.id.register_etLName);
        etLogin = findViewById(R.id.register_etLogin);
        etPsw = findViewById(R.id.register_etPsw);

        btnRegister = findViewById(R.id.register_btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fName = etFName.getText().toString();
                String lName = etLName.getText().toString();
                String login = etLogin.getText().toString();
                String psw = etPsw.getText().toString();

                // TODO: adicionar um layout mais intuitivo de alertar o usuario, sem ser o Toast.

                if(fName.isEmpty() || lName.isEmpty() || login.isEmpty() || psw.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Preencha o formulário corretamente.", Toast.LENGTH_LONG).show();
                    return;
                }
                else if(!fName.matches("^([A-Za-z])+$") || !lName.matches("^([A-Za-z])+$")){
                    Toast.makeText(RegisterActivity.this, "Nomes com caracteres inválidos.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(psw.length() < 8){
                    Toast.makeText(RegisterActivity.this, "Sua senha deve ter 8 caracteres.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (!psw.matches("^(?=.*[A-Z]).+$")){
                    Toast.makeText(RegisterActivity.this, "Sua senha deve ter uma letra maiúscula", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    // Supostamente tudo certo
                    registerUser(fName, lName, login, psw);
                    btnRegister.setEnabled(false);
                    btnRegister.setAlpha(0.3f);
                }
            }
        });

        btnSeePsw = findViewById(R.id.register_btnSeePsw);
        btnSeePsw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tipoAtual = etPsw.getInputType();

                if ((tipoAtual & InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    // esconder campo
                    etPsw.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    btnSeePsw.setImageResource(R.drawable.ic_eye_off);
                } else {
                    // mostrar campo
                    etPsw.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    btnSeePsw.setImageResource(R.drawable.ic_eye_on);
                }

                etPsw.setSelection(etPsw.getText().length());
            }
        });

        ScrollView scrollView = findViewById(R.id.scroll);
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                scrollView.getWindowVisibleDisplayFrame(r);
                int screenHeight = scrollView.getRootView().getHeight();

                // r.bottom is the position above soft keypad or device button.
                // if keypad is shown, the r.bottom is smaller than that before.
                int keypadHeight = screenHeight - r.bottom;

                if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                    if(etLogin.isFocused()){
                        scrollView.setTranslationY(keypadHeight*(-0.2f));
                    }
                    if(etPsw.isFocused()){
                        scrollView.setTranslationY(keypadHeight*(-0.6f));
                    }
                }
                else {
                    scrollView.setTranslationY(0f);
                }
            }
        });
    }

    public void registerUser(String fName, String lName, String login, String psw){

        // TODO: adicionar um layout mais intuitivo de alertar o usuario, sem ser o Toast.

        auth.createUserWithEmailAndPassword(login, psw).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                // recupera o token de autenticação
                FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                mUser.getIdToken(true)
                        .addOnCompleteListener(taskToken -> {
                            if (taskToken.isSuccessful()) {
                                String token = taskToken.getResult().getToken();

                                SendUserToAPI suta = new SendUserToAPI(token, fName, lName);
                                suta.execute();
                            } else {
                                Toast.makeText(RegisterActivity.this, "Erro ao recuperar token", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            else{
                Exception e = task.getException();
                Toast.makeText(this, formatFirebaseException(e), Toast.LENGTH_SHORT).show();

                btnRegister.setActivated(true);
                btnRegister.setAlpha(1f);
            }
        });
    }

    public String formatFirebaseException(Exception e){
        if(e instanceof FirebaseAuthInvalidCredentialsException &&
                e.getMessage() != null &&
                e.getMessage().contains("The email address is badly formatted")){
            return "Formato de e-mail inválido.";
        }
        if(e instanceof FirebaseAuthUserCollisionException){
            return "E-mail já cadastrado.";
        }
        else{
            return "Erro desconhecido: " + e.getMessage();
        }
    }

    public class SendUserToAPI extends AsyncTask<String, Void, String>{
        private String token, fName, lName;

        public SendUserToAPI(String token, String fName, String lName) {
            this.token = token;
            this.fName = fName;
            this.lName = lName;
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpsURLConnection conn;
            try {
                URL url = new URL("https://portavoz.onrender.com/api/v1/users/auth");

                conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("fName", fName);
                jsonParam.put("lName", lName);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(jsonParam.toString());
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                InputStream is;

                if (responseCode >= 200 && responseCode < 300) {
                    is = conn.getInputStream();
                } else {
                    is = conn.getErrorStream();
                }

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

            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (ProtocolException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                Log.e("API_ERROR", "Erro ao enviar usuário", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s){
            startActivity(new Intent(RegisterActivity.this, FeedActivity.class));
            finish();
        }
    }
}