package com.example.portavoz.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.portavoz.FeedActivity;
import com.example.portavoz.MainActivity;
import com.example.portavoz.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    Button btnLogin, btnReturn;
    ImageButton btnSeePsw;
    EditText etLogin, etPsw;
    TextView txtForgotPssw;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etLogin = findViewById(R.id.login_etLogin);
        etPsw = findViewById(R.id.login_etPsw);

        btnLogin = findViewById(R.id.login_btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String login = etLogin.getText().toString();
                String pssw = etPsw.getText().toString();

                // TODO: adicionar um layout mais intuitivo de alertar o usuario, sem ser o Toast.
                if(login.isEmpty() || pssw.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Preencha o formulário corretamente", Toast.LENGTH_LONG).show();
                    return;
                }

                loginUser(login, pssw);
            }
        });

        btnReturn = findViewById(R.id.login_btnReturn);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        });

        btnSeePsw = findViewById(R.id.login_btnSeePsw);
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

        txtForgotPssw = findViewById(R.id.login_txtForgotPsw);
        txtForgotPssw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPsswActivity.class));
                finish();
            }
        });
    }

    public void loginUser(String login, String pssw){
        auth.signInWithEmailAndPassword(login, pssw).addOnCompleteListener(this, task -> {
            if(task.isSuccessful()){
                startActivity(new Intent(LoginActivity.this, FeedActivity.class));
            }
            else{
                Exception e = task.getException();

                // TODO: adicionar um layout mais intuitivo de alertar o usuario, sem ser o Toast.

                Toast.makeText(this, formatFirebaseException(e), Toast.LENGTH_LONG).show();
            }
        });
    }

    public String formatFirebaseException(Exception e){
        if(e instanceof FirebaseAuthInvalidCredentialsException &&
                e.getMessage() != null &&
                e.getMessage().contains("The email address is badly formatted")){
            return "Formato de e-mail inválido";
        }
        if(e instanceof FirebaseAuthInvalidUserException) {
            return "Usuário não encontrado";
        }
        else if(e instanceof FirebaseAuthInvalidCredentialsException){
            return "Login ou senha incorretos";
        }
        else{
            return "Erro desconhecido: " + e.getMessage();
        }
    }
}