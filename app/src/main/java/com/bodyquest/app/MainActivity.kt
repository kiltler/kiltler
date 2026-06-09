package com.bodyquest.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bodyquest.app.ui.BodyQuestRoot
import com.bodyquest.app.ui.BodyQuestViewModel
import com.bodyquest.app.ui.theme.BodyQuestTheme

class MainActivity : ComponentActivity() {

    private val requestNotifications =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val container = (application as BodyQuestApp).container

        setContent {
            BodyQuestTheme {
                Surface(Modifier.fillMaxSize()) {
                    val vm: BodyQuestViewModel = viewModel(
                        factory = BodyQuestViewModel.Factory(
                            container.repository,
                            container.reminderScheduler,
                            applicationContext,
                        )
                    )
                    BodyQuestRoot(vm, Modifier.systemBarsPadding())
                }
            }
        }
    }
}
