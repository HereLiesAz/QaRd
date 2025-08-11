package com.hereliesaz.qrlockscreen.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.hereliesaz.qrlockscreen.ui.theme.QrLockscreenTheme

class LockscreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QrLockscreenTheme {
                Text("This is the lock screen activity.")
            }
        }
    }
}
