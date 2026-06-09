package com.bodyquest.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bodyquest.app.domain.seed.MealIdeas
import com.bodyquest.app.ui.AppUiState
import com.bodyquest.app.ui.components.BqCard
import com.bodyquest.app.ui.components.KeyValueRow
import com.bodyquest.app.ui.components.SectionTitle
import com.bodyquest.app.ui.components.StatPill
import com.bodyquest.app.ui.components.XpBar
import com.bodyquest.app.ui.theme.AttrEndurance
import com.bodyquest.app.ui.theme.BqSecondary
import com.bodyquest.app.ui.theme.BqTertiary

@Composable
fun NutritionScreen(
    state: AppUiState,
    onAddWater: (Int) -> Unit,
    onResetWater: () -> Unit,
) {
    val plan = state.nutrition ?: return
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Питание", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Text("Здоровый подход к рекомпозиции. Без жёстких диет.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        BqCard(Modifier.fillMaxWidth()) {
            SectionTitle("⚡ Энергия дня")
            Text("${plan.targetCalories} ккал", style = MaterialTheme.typography.headlineLarge,
                color = BqTertiary, fontWeight = FontWeight.Black)
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatPill("Белки", "${plan.macros.proteinG} г", Modifier.weight(1f), AttrEndurance)
                StatPill("Жиры", "${plan.macros.fatG} г", Modifier.weight(1f), BqTertiary)
                StatPill("Углеводы", "${plan.macros.carbsG} г", Modifier.weight(1f), BqSecondary)
            }
            Column(Modifier.padding(top = 10.dp)) {
                KeyValueRow("BMR (база покоя)", "${plan.bmr} ккал")
                KeyValueRow("TDEE (расход)", "${plan.tdee} ккал")
                if (plan.deficitApplied) KeyValueRow("Дефицит", "−${plan.deficitKcal} ккал")
            }
            if (plan.flooredToFloor) {
                Text("⚠️ Цель поднята до безопасного порога 1700 ккал. Ниже опускаться нельзя.",
                    style = MaterialTheme.typography.bodySmall, color = BqTertiary, modifier = Modifier.padding(top = 6.dp))
            } else if (plan.flooredToBmr) {
                Text("⚠️ Цель не опускается ниже BMR — это защищает мышцы и обмен веществ.",
                    style = MaterialTheme.typography.bodySmall, color = BqTertiary, modifier = Modifier.padding(top = 6.dp))
            }
        }

        BqCard(Modifier.fillMaxWidth()) {
            SectionTitle("💧 Трекер воды")
            val goal = plan.waterMlGoal
            val frac = if (goal > 0) state.waterMl.toFloat() / goal else 0f
            Text("${state.waterMl} / $goal мл", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            XpBar(frac, color = AttrEndurance, modifier = Modifier.padding(vertical = 8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onAddWater(250) }, modifier = Modifier.weight(1f)) { Text("+250 мл") }
                OutlinedButton(onClick = { onAddWater(500) }, modifier = Modifier.weight(1f)) { Text("+500 мл") }
                OutlinedButton(onClick = onResetWater, modifier = Modifier.weight(1f)) { Text("Сброс") }
            }
        }

        SectionTitle("Идеи блюд (упор на белок)")
        MealIdeas.all.forEach { meal ->
            BqCard(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(meal.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f))
                    Text("${meal.kcal} ккал", color = BqTertiary, fontWeight = FontWeight.Bold)
                }
                Text("Белок: ${meal.proteinG} г", style = MaterialTheme.typography.labelMedium, color = AttrEndurance)
                Text(meal.description, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
