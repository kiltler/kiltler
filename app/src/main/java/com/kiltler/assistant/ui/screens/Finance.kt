package com.kiltler.assistant.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kiltler.assistant.data.Order
import com.kiltler.assistant.data.OrderStatus
import com.kiltler.assistant.ui.AppSettings
import java.util.Calendar

private const val WEEK_MS = 7L * 24 * 60 * 60 * 1000

/** Финансовые итоги за период. */
data class FinanceStats(
    val revenue: Double,
    val materials: Double,
    val ads: Double,
    val net: Double
)

private fun computeFinance(
    orders: List<Order>,
    sinceMillis: Long?,
    settings: AppSettings
): FinanceStats {
    val revenue = orders
        .filter { OrderStatus.from(it.status) == OrderStatus.DONE && it.price > 0 }
        .filter { sinceMillis == null || (it.scheduledMillis ?: it.createdAt) >= sinceMillis }
        .sumOf { o ->
            val saleQty = WorkType.parse(o.description)[WorkType.SALE] ?: 0
            val acBase = if (saleQty > 0) settings.acPriceFor(o.acModel) * saleQty else 0.0
            (o.price - acBase).coerceAtLeast(0.0)
        }
    val materials = revenue * settings.materialsPct / 100.0
    val ads = revenue * settings.adsPct / 100.0
    return FinanceStats(revenue, materials, ads, revenue - materials - ads)
}

private fun startOfToday(): Long {
    val c = Calendar.getInstance()
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

private fun money(value: Double): String =
    "%,d ₽".format(value.toLong()).replace(',', ' ')

/** Карточка финансов вверху расписания — чистая прибыль и переход к деталям. */
@Composable
fun FinanceCard(
    orders: List<Order>,
    settings: AppSettings,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val all = computeFinance(orders, null, settings)
    val day = computeFinance(orders, startOfToday(), settings)
    val week = computeFinance(orders, System.currentTimeMillis() - WEEK_MS, settings)

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Payments, contentDescription = null)
                Text(
                    "  Финансы",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(6.dp))
            Text("Чистая прибыль за всё время", style = MaterialTheme.typography.bodySmall)
            Text(
                money(all.net),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FinanceMini("Сегодня", day.net, Modifier.weight(1f))
                FinanceMini("За неделю", week.net, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FinanceMini(label: String, value: Double, modifier: Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(
            money(value),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Подробный расчёт финансов с настройкой отчислений. */
@Composable
fun FinanceDialog(
    orders: List<Order>,
    settings: AppSettings,
    onDismiss: () -> Unit,
    onSave: (Int, Int) -> Unit
) {
    var mat by remember { mutableStateOf(settings.materialsPct.toString()) }
    var ads by remember { mutableStateOf(settings.adsPct.toString()) }
    val m = mat.toIntOrNull()?.coerceIn(0, 100) ?: 0
    val a = ads.toIntOrNull()?.coerceIn(0, 100) ?: 0
    val effective = settings.copy(materialsPct = m, adsPct = a)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Финансы") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FinanceSection("Сегодня", computeFinance(orders, startOfToday(), effective))
                FinanceSection(
                    "За неделю",
                    computeFinance(orders, System.currentTimeMillis() - WEEK_MS, effective)
                )
                FinanceSection("За всё время", computeFinance(orders, null, effective))

                HorizontalDivider()
                Text(
                    "Отчисления (% от выручки)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = mat,
                        onValueChange = { mat = it.filter(Char::isDigit).take(3) },
                        label = { Text("Материалы, %") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = ads,
                        onValueChange = { ads = it.filter(Char::isDigit).take(3) },
                        label = { Text("Реклама, %") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    "Считается по выполненным заказам.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(m, a) }) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        }
    )
}

@Composable
private fun FinanceSection(title: String, stats: FinanceStats) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        FinanceRow("Выручка", money(stats.revenue))
        FinanceRow("На материалы", "− ${money(stats.materials)}")
        FinanceRow("На рекламу", "− ${money(stats.ads)}")
        FinanceRow("Чистая прибыль", money(stats.net), highlight = true)
    }
}

@Composable
private fun FinanceRow(label: String, value: String, highlight: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            color = if (highlight) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}
