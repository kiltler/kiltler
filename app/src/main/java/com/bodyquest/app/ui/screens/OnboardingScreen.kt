package com.bodyquest.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bodyquest.app.domain.ActivityLevel
import com.bodyquest.app.domain.BodyInput
import com.bodyquest.app.domain.Equipment
import com.bodyquest.app.ui.components.BqCard
import com.bodyquest.app.ui.components.SectionTitle
import com.bodyquest.app.ui.theme.BqTertiary

@Composable
fun NumberField(label: String, value: String, onValue: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValue(it.replace(',', '.').filter { c -> c.isDigit() || c == '.' }) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
    )
}

@Composable
fun OnboardingScreen(onFinish: (
    name: String, height: Int, age: Int, days: Int, activity: ActivityLevel,
    weight: Double, chest: Double, shoulders: Double, belly: Double, waist: Double,
    thigh: Double, hips: Double, inseam: Double, foot: Double,
) -> Unit) {
    var name by remember { mutableStateOf("Герой") }
    var height by remember { mutableStateOf("189") }
    var weight by remember { mutableStateOf("97") }
    var age by remember { mutableStateOf("30") }
    var chest by remember { mutableStateOf("109") }
    var shoulders by remember { mutableStateOf("49") }
    var belly by remember { mutableStateOf("107") }
    var waist by remember { mutableStateOf("97") }
    var thigh by remember { mutableStateOf("66") }
    var hips by remember { mutableStateOf("110") }
    var inseam by remember { mutableStateOf("90") }
    var foot by remember { mutableStateOf("27") }
    var days by remember { mutableStateOf(3) }

    fun d(s: String, def: Double) = s.toDoubleOrNull() ?: def
    fun i(s: String, def: Int) = s.toIntOrNull() ?: def

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("BodyQuest", style = MaterialTheme.typography.headlineLarge, color = BqTertiary, fontWeight = FontWeight.Black)
        Text(
            "Создай героя и прокачай его от новобранца до легенды. Все данные хранятся только на телефоне.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        BqCard {
            SectionTitle("Параметры героя")
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Имя героя") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("Рост, см", height, { height = it }, Modifier.weight(1f))
                NumberField("Вес, кг", weight, { weight = it }, Modifier.weight(1f))
                NumberField("Возраст", age, { age = it }, Modifier.weight(1f))
            }
        }

        BqCard {
            SectionTitle("Обхваты, см")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("Грудь", chest, { chest = it }, Modifier.weight(1f))
                NumberField("Плечи (ширина)", shoulders, { shoulders = it }, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("Живот", belly, { belly = it }, Modifier.weight(1f))
                NumberField("Талия", waist, { waist = it }, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("Бедро", thigh, { thigh = it }, Modifier.weight(1f))
                NumberField("Обхват бёдер", hips, { hips = it }, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("Inseam", inseam, { inseam = it }, Modifier.weight(1f))
                NumberField("Стопа", foot, { foot = it }, Modifier.weight(1f))
            }
        }

        BqCard {
            SectionTitle("Инвентарь")
            Text("Тренировки строятся только на этом снаряжении:",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(Modifier.padding(top = 6.dp)) {
                Equipment.entries.filter { it != Equipment.BODYWEIGHT }.forEach {
                    Text("✓ ${it.title}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        BqCard {
            SectionTitle("Интенсивность")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = days == 3, onClick = { days = 3 }, label = { Text("3 трен./нед") })
                FilterChip(selected = days == 4, onClick = { days = 4 }, label = { Text("4 трен./нед") })
            }
        }

        BqCard {
            SectionTitle("Дисклеймер")
            Text(
                "Перед началом проконсультируйтесь с врачом. Не тренируйтесь через боль. " +
                    "Техника важнее веса. Прогресс — это марафон, а не спринт.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Button(
            onClick = {
                val activity = if (days >= 4) ActivityLevel.MODERATE else ActivityLevel.LIGHT
                onFinish(
                    name.ifBlank { "Герой" },
                    BodyInput.height(i(height, 189)), BodyInput.age(i(age, 30)), days, activity,
                    BodyInput.weight(d(weight, 97.0)), BodyInput.circumference(d(chest, 109.0)),
                    BodyInput.shoulders(d(shoulders, 49.0)), BodyInput.circumference(d(belly, 107.0)),
                    BodyInput.circumference(d(waist, 97.0)), BodyInput.thigh(d(thigh, 66.0)),
                    BodyInput.circumference(d(hips, 110.0)), BodyInput.inseam(d(inseam, 90.0)),
                    BodyInput.foot(d(foot, 27.0)),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Начать путь героя", fontWeight = FontWeight.Bold) }
    }
}
