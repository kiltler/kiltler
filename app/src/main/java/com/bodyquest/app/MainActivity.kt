package com.bodyquest.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bodyquest.app.ui.BodyQuestRoot
import com.bodyquest.app.ui.BodyQuestViewModel
import com.bodyquest.app.ui.theme.BodyQuestTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                    NotificationPermissionPrompt()
                }
            }
        }
    }
}

/** Одноразовый диалог-объяснение перед запросом разрешения на уведомления (Android 13+). */
@Composable
private fun NotificationPermissionPrompt() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = LocalContext.current
    val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

    var show by remember { mutableStateOf(!granted) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* результат не важен — пользователь решил */ }

    if (show) {
        AlertDialog(
            onDismissRequest = { show = false },
            confirmButton = {
                TextButton(onClick = {
                    show = false
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }) { Text("Разрешить") }
            },
            dismissButton = { TextButton(onClick = { show = false }) { Text("Не сейчас") } },
            title = { Text("Включить напоминания?") },
            text = {
                Text(
                    "BodyQuest будет напоминать о тренировке дня, воде и о том, что серия " +
                        "под угрозой. Без интернета, только локальные уведомления.",
                )
            },
        )
    }
}
