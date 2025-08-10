package com.example.qrlockscreen;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class LockScreenActivity extends AppCompatActivity {

    private ImageView qrCodeImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make the activity full-screen and show over the lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);


        setContentView(R.layout.activity_lock_screen);

        qrCodeImageView = findViewById(R.id.qr_code_image_view);

        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        String qrCodeData = prefs.getString(MainActivity.QR_CODE_DATA, null);

        if (qrCodeData != null) {
            Bitmap qrCodeBitmap = QRCodeGenerator.generateQRCodeBitmap(qrCodeData, 300, 300);
            if (qrCodeBitmap != null) {
                qrCodeImageView.setImageBitmap(qrCodeBitmap);
            }
        }

        // Handle tap to unlock
        findViewById(android.R.id.content).setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                keyguardManager.requestDismissKeyguard(this, new KeyguardManager.KeyguardDismissCallback() {
                    @Override
                    public void onDismissSucceeded() {
                        super.onDismissSucceeded();
                        finish();
                    }
                });
            } else {
                // For older versions, we can't easily integrate with the keyguard,
                // so we just finish the activity. This is not ideal but a limitation of older APIs.
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Do nothing to prevent dismissing the lock screen
    }
}
