package com.example.portavoz;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

public class CreatePost extends AppCompatActivity {

    //pega o fragmento atual
    int curFragment = 1;

    Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_post);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnNext = findViewById(R.id.btnNext);

        getSupportFragmentManager().
                beginTransaction().
                replace(R.id.fragmentContainer, new ContentFragment()).
                commit();

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nxtFragment();
            }
        });

    }

    private void nxtFragment() {
        Fragment fragment = null;

        switch (curFragment) {
            case 1:
                fragment = new imageFragment();
                break;
            case 2:
                fragment = new TagFragment();
                break;
            case 3:
                fragment = new MapFragment();
                break;
        }

        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in , android.R.anim.fade_out)
                    .replace(R.id.fragmentContainer, fragment).commit();

            curFragment++;
        } else {
            Toast.makeText(this, "Finalizado", Toast.LENGTH_SHORT).show();
        }
    }
}