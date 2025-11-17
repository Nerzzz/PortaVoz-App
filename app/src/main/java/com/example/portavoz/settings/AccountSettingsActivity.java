package com.example.portavoz.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.portavoz.FeedActivity;
import com.example.portavoz.MainActivity;
import com.example.portavoz.R;
import com.example.portavoz.profile.EditProfileActivity;
import com.example.portavoz.settings.account.ChangeEmailActivity;
import com.example.portavoz.settings.account.ChangePasswordActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button changeEmail = findViewById(R.id.accSett_btnChangeEmail);
        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AccountSettingsActivity.this, ChangeEmailActivity.class));
            }
        });

        Button changePssw = findViewById(R.id.accSett_btnChangePssw);
        changePssw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AccountSettingsActivity.this, ChangePasswordActivity.class));
            }
        });

        Button btnReturn = findViewById(R.id.accSett_btnReturn);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button btnLogout = findViewById(R.id.accSett_btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = LayoutInflater.from(AccountSettingsActivity.this).inflate(R.layout.alert_confirmation, null);

                TextView title = dialogView.findViewById(R.id.confirmation_txtTitle);
                title.setText("Fazer Logout");

                TextView desc = dialogView.findViewById(R.id.confirmation_txtDesc);
                desc.setText("Tem certeza que deseja sair? Você terá que fazer login novamente se quiser utilizar sua conta.");

                Button confirm = dialogView.findViewById(R.id.confirmation_btnConfirm);
                Button cancel = dialogView.findViewById(R.id.confirmation_btnCancel);

                confirm.setText("Sair");
                cancel.setText("Não");

                AlertDialog.Builder builder = new AlertDialog.Builder(AccountSettingsActivity.this);
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        mAuth.signOut();

                        Intent intent = new Intent(AccountSettingsActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

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
    }
}