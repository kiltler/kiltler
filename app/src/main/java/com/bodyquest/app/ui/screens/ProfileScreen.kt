package com.bodyquest.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bodyquest.app.data.SettingsEntity
import com.bodyquest.app.data.UserProfileEntity
import com.bodyquest.app.domain.ActivityLevel
import com.bodyquest.app.domain.BodyInput
import com.bodyquest.app.ui.AppUiState
import com.bodyquest.app.ui.components.BqCard
import com.bodyquest.app.ui.components.DeletableHistoryRow
import com.bodyquest.app.ui.components.epochDayLabel
import com.bodyquest.app.ui.components.SectionTitle
import com.bodyquest.app.ui.theme.BqDanger

@Composable
fun ProfileScreen(
    state: AppUiState,
    settings: SettingsEntity?,
    onUpdateProfile: (UserProfileEntity) -> Unit,
    onLogMeasurement: (Double, Double, Double, Double, Double, Double, Double, Double, Double) -> Unit,
    onWorkoutReminder: (Boolean, Int, Int) -> Unit,
    onWaterReminder: (Boolean) -> Unit,
    onExport: (Uri) -> Unit,
    onImport: (Uri) -> Unit,
    onDeleteMeasurement: (Long) -> Unit,
    onShare: () -> Unit,
    onReset: () -> Unit,
) {
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let(onExport) }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(onImport) }

    val profile = state.profile ?: return
    val latest = state.latest

    var name by remember(profile.id) { mutableStateOf(profile.name) }
    var height by remember(profile.id) { mutableStateOf(profile.heightCm.toString()) }
    var age by remember(profile.id) { mutableStateOf(profile.age.toString()) }
    var days by remember(profile.id) { mutableStateOf(profile.daysPerWeek) }
    var activity by remember(profile.id) {
        mutableStateOf(runCatching { ActivityLevel.valueOf(profile.activity) }.getOrDefault(ActivityLevel.LIGHT))
    }

    // Поля замеров
    var weight by remember(latest?.id) { mutableStateOf((latest?.weightKg ?: 97.0).toString()) }
    var chest by remember(latest?.id) { mutableStateOf((latest?.chest ?: 109.0).toString()) }
    var shoulders by remember(latest?.id) { mutableStateOf((latest?.shoulders ?: 49.0).toString()) }
    var belly by remember(latest?.id) { mutableStateOf((latest?.belly ?: 107.0).toString()) }
    var waist by remember(latest?.id) { mutableStateOf((latest?.waist ?: 97.0).toString()) }
    var thigh by remember(latest?.id) { mutableStateOf((latest?.thigh ?: 66.0).toString()) }
    var hips by remember(latest?.id) { mutableStateOf((latest?.hips ?: 110.0).toString()) }
    var inseam by remember(latest?.id) { mutableStateOf((latest?.inseam ?: 90.0).toString()) }
    var foot by remember(latest?.id) { mutableStateOf((latest?.foot ?: 27.0).toString()) }

    var showReset by remember { mutableStateOf(false) }
    fun d(s: String, def: Double) = s.toDoubleOrNull() ?: def

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Профиль и настройки", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)

        BqCard(Modifier.fillMaxWidth()) {
            SectionTitle("Герой")
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Имя героя") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("Рост, см", height, { height = it }, Modifier.weight(1f))
                NumberField("Возраст", age, { age = it }, Modifier.weight(1f))
            }
            Text("Интенсивность", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = days == 3, onClick = { days = 3 }, label = { Text("3×") })
                FilterChip(selected = days == 4, onClick = { days = 4 }, label = { Text("4×") })
            }
            Text("Активность (для калорий)", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = activity == ActivityLevel.LIGHT, onClick = { activity = ActivityLevel.LIGHT },
                    label = { Text("Лёгкая") })
                FilterChip(selected = activity == ActivityLevel.MODERATE, onClick = { activity = ActivityLevel.MODERATE },
                    label = { Text("Умеренная") })
            }
            Button(
                onClick = {
                    onUpdateProfile(
                        profile.copy(
                            name = name.ifBlank { "Герой" },
                            heightCm = BodyInput.height(height.toIntOrNull() ?: profile.heightCm),
                            age = BodyInput.age(age.toIntOrNull() ?: profile.age),
                            daysPerWeek = days,
                            activity = activity.name,
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            ) { Text("Сохранить профиль") }
        }

        BqCard(Modifier.fillMaxWidth()) {
            SectionTitle("Новые замеры")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("Вес, кг", weight, { weight = it }, Modifier.weight(1f))
                NumberField("Талия", waist, { waist = it }, Modifier.weight(1f))
                NumberField("Живот", belly, { belly = it }, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("Грудь", chest, { chest = it }, Modifier.weight(1f))
                NumberField("Плечи", shoulders, { shoulders = it }, Modifier.weight(1f))
                NumberField("Бедро", thigh, { thigh = it }, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("Обхв. бёдер", hips, { hips = it }, Modifier.weight(1f))
                NumberField("Inseam", inseam, { inseam = it }, Modifier.weight(1f))
                NumberField("Стопа", foot, { foot = it }, Modifier.weight(1f))
            }
            Button(
                onClick = {
                    onLogMeasurement(
                        BodyInput.weight(d(weight, 97.0)), BodyInput.circumference(d(chest, 109.0)),
                        BodyInput.shoulders(d(shoulders, 49.0)), BodyInput.circumference(d(belly, 107.0)),
                        BodyInput.circumference(d(waist, 97.0)), BodyInput.thigh(d(thigh, 66.0)),
                        BodyInput.circumference(d(hips, 110.0)), BodyInput.inseam(d(inseam, 90.0)),
                        BodyInput.foot(d(foot, 27.0)),
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            ) { Text("Записать замеры (+XP композиции)") }
        }

        if (state.measurements.isNotEmpty()) {
            BqCard(Modifier.fillMaxWidth()) {
                SectionTitle("История замеров")
                state.measurements.sortedByDescending { it.dateMillis }.take(20).forEach { mm ->
                    DeletableHistoryRow(
                        title = "Вес ${mm.weightKg} кг · талия ${mm.waist} см",
                        subtitle = epochDayLabel(mm.dateEpochDay),
                        onDelete = { onDeleteMeasurement(mm.id) },
                    )
                }
            }
        }

        BqCard(Modifier.fillMaxWidth()) {
            SectionTitle("Напоминания")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Напоминание о тренировке")
                Switch(
                    checked = settings?.remindersEnabled == true,
                    onCheckedChange = {
                        onWorkoutReminder(it, settings?.reminderHour ?: 18, settings?.reminderMinute ?: 30)
                    },
                )
            }
            Text("Время: ${"%02d".format(settings?.reminderHour ?: 18)}:${"%02d".format(settings?.reminderMinute ?: 30)}",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Напоминание пить воду")
                Switch(
                    checked = settings?.waterRemindersEnabled == true,
                    onCheckedChange = { onWaterReminder(it) },
                )
            }
        }

        BqCard(Modifier.fillMaxWidth()) {
            SectionTitle("Резервная копия прогресса")
            Text("Сохрани прогресс в файл и положи его в любое облако (Google Drive и т.п.). " +
                "После переустановки — импортируй обратно.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(
                onClick = { exportLauncher.launch("bodyquest_backup.json") },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            ) { Text("Экспорт прогресса в файл") }
            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/json", "text/*", "*/*")) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) { Text("Импорт прогресса из файла") }
            OutlinedButton(
                onClick = onShare,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) { Text("Поделиться прогрессом (картинка)") }
        }

        BqCard(Modifier.fillMaxWidth()) {
            SectionTitle("Данные")
            Text("Всё хранится локально (Room). Google Auto Backup автоматически бэкапит базу " +
                "на твой аккаунт и восстанавливает её при переустановке.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(
                onClick = { showReset = true },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BqDanger),
            ) { Text("Сбросить прогресс") }
        }
    }

    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            confirmButton = {
                TextButton(onClick = { showReset = false; onReset() }) {
                    Text("Сбросить", color = BqDanger)
                }
            },
            dismissButton = { TextButton(onClick = { showReset = false }) { Text("Отмена") } },
            title = { Text("Сбросить прогресс?") },
            text = {
                Text("XP, уровни, тренировки, рекорды, серия и достижения обнулятся. " +
                    "Текущие замеры останутся точкой отсчёта. Профиль сохранится.")
            },
        )
    }
}
