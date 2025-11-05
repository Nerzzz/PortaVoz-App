package com.example.portavoz;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.os.BuildCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    TextView txtVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtVersion = findViewById(R.id.splash_txtVersion);

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

            String version = pInfo.versionName;
            int vCode = pInfo.versionCode;

            String type = BuildConfig.BUILD_TYPE;

            txtVersion.setText("Versão "+version+" "+type+" (build "+vCode+")");
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(hasInternet(SplashActivity.this)){
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if(currentUser != null){
                        startActivity(new Intent(SplashActivity.this, FeedActivity.class));
                    }
                    else{
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    }
                }
                else{
                    startActivity(new Intent(SplashActivity.this, NoInternetActivity.class));
                }
                finish();
            }
        }, 2000);
    }

    public boolean hasInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }
}