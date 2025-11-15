package com.example.portavoz.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.portavoz.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ResetPsswActivity extends AppCompatActivity {

    EditText etEmail;
    Button btnReturn, btnSend;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_activity_reset_pssw);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etEmail = findViewById(R.id.resetPsw_etEmail);

        btnReturn = findViewById(R.id.resetPsw_btnReturn);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSend = findViewById(R.id.resetPsw_btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(etEmail.getText().toString().isEmpty()){
                    Toast.makeText(ResetPsswActivity.this, "O e-mail deve ser informado antes!", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnSend.setEnabled(false);
                btnSend.setAlpha(0.3f);

                firebaseAuth = FirebaseAuth.getInstance();
                firebaseAuth.sendPasswordResetEmail(etEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(ResetPsswActivity.this, "E-mail de redefinição enviado. Confira sua caixa de e-mail.", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(ResetPsswActivity.this, formatFirebaseException(task.getException()), Toast.LENGTH_SHORT).show();
                            btnSend.setEnabled(true);
                            btnSend.setAlpha(1f);
                        }
                    }
                });
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
        else{
            return "Erro desconhecido: " + e.getMessage();
        }
    }
}