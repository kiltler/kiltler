package com.kiltler.assistant.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Диалог облачной синхронизации: общий код связывает несколько телефонов
 * с одним набором данных.
 */
@Composable
fun SyncDialog(
    currentCode: String?,
    onDismiss: () -> Unit,
    onEnable: (String) -> Unit,
    onDisable: () -> Unit
) {
    var code by remember { mutableStateOf(currentCode ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Синхронизация") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Введите один и тот же код на этом телефоне и на втором — " +
                        "заказы, напоминания и материалы будут синхронизироваться " +
                        "через интернет автоматически.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.trim() },
                    label = { Text("Код синхронизации") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(onClick = { code = "kiltler-" + (1000..9999).random() }) {
                    Text("Сгенерировать код")
                }
                if (currentCode != null) {
                    Text(
                        "Синхронизация включена: $currentCode",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        "Внимание: при включении данные второго телефона " +
                            "заменяются общими.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = code.isNotBlank(),
                onClick = { onEnable(code.trim()) }
            ) { Text("Включить") }
        },
        dismissButton = {
            if (currentCode != null) {
                TextButton(onClick = onDisable) { Text("Выключить") }
            } else {
                TextButton(onClick = onDismiss) { Text("Отмена") }
            }
        }
    )
}
