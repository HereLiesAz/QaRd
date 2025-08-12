package com.hereliesaz.qard.ui

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hereliesaz.qard.R
import com.hereliesaz.qard.data.QrDataStore
import com.hereliesaz.qard.widget.QrGenerator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LockscreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_screen)

        val qrCodeImageView: ImageView = findViewById(R.id.qr_code_image_view)
        val unlockTextView: TextView = findViewById(R.id.unlock_text)
        val dataStore = QrDataStore(this)

        lifecycleScope.launch {
            val savedConfigs = dataStore.getSavedConfigs().first()
            val latestConfig = savedConfigs.lastOrNull()

            if (latestConfig != null) {
                val qrBitmap = QrGenerator.generate(latestConfig)
                if (qrBitmap != null) {
                    qrCodeImageView.setImageBitmap(qrBitmap)
                } else {
                    unlockTextView.text = "Error generating QR code."
                }
            } else {
                unlockTextView.text = "No QR code configured. Please create one first."
                Toast.makeText(this@LockscreenActivity, "No QR code configured", Toast.LENGTH_LONG).show()
            }
        }
    }
}
