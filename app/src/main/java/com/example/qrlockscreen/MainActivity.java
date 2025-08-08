package com.example.qrlockscreen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_OVERLAY_PERMISSION = 123;
    public static final String QR_CODE_DATA = "qr_code_data";
    public static final String PREFS_NAME = "qrlockscreen_prefs";

    private EditText qrCodeDataEditText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        qrCodeDataEditText = findViewById(R.id.qr_code_data_edit_text);
        saveButton = findViewById(R.id.save_button);

        saveButton.setOnClickListener(v -> {
            String data = qrCodeDataEditText.getText().toString().trim();
            if (!data.isEmpty()) {
                saveData(data);
                Toast.makeText(this, "QR Code data saved!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enter some data", Toast.LENGTH_SHORT).show();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
        } else {
            startLockScreenService();
        }
    }

    private void saveData(String data) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(QR_CODE_DATA, data);
        editor.apply();
    }

    private void startLockScreenService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, LockScreenService.class));
        } else {
            startService(new Intent(this, LockScreenService.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    startLockScreenService();
                } else {
                    Toast.makeText(this, "Overlay permission is required for the lock screen to work", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
