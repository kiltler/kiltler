package com.kiltler.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kiltler.assistant.data.Order
import com.kiltler.assistant.data.OrderStatus
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

private const val OVERLOAD_HOURS = 12.0   // максимум для дня (9–21)
private const val NOISY_LIMIT = 10.0      // 9–13 + 15–21, без «тихого часа»
private const val HIGH_HOURS = 8.0
private const val MEDIUM_HOURS = 4.0

enum class LoadLevel { FREE, LOW, MEDIUM, HIGH, OVERLOAD }

/** Сводка загрузки на один день. */
data class DayLoad(
    val total: Double,
    val noisy: Double,
    val byType: Map<WorkType, Double>,
    val orderCount: Int
) {
    val dominant: WorkType? get() = byType.maxByOrNull { it.value }?.key
    val level: LoadLevel get() = when {
        total == 0.0 -> LoadLevel.FREE
        noisy > NOISY_LIMIT || total > OVERLOAD_HOURS -> LoadLevel.OVERLOAD
        total > HIGH_HOURS -> LoadLevel.HIGH
        total > MEDIUM_HOURS -> LoadLevel.MEDIUM
        else -> LoadLevel.LOW
    }
}

private fun activeOrdersOnDay(orders: List<Order>, date: LocalDate): List<Order> {
    val zone = ZoneId.systemDefault()
    val dayStart = date.atStartOfDay(zone).toInstant().toEpochMilli()
    val dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    return orders.filter { o ->
        val s = OrderStatus.from(o.status)
        s != OrderStatus.DONE && s != OrderStatus.CANCELLED &&
            (o.scheduledMillis?.let { it in dayStart until dayEnd } == true)
    }
}

fun computeDayLoad(orders: List<Order>, date: LocalDate): DayLoad {
    val dayOrders = activeOrdersOnDay(orders, date)
    val byType = mutableMapOf<WorkType, Double>()
    var total = 0.0
    var noisy = 0.0
    for (order in dayOrders) {
        for (type in WorkType.parse(order.description)) {
            if (type.hours <= 0.0) continue
            byType[type] = (byType[type] ?: 0.0) + type.hours
            total += type.hours
            if (type.noisy) noisy += type.hours
        }
    }
    return DayLoad(total, noisy, byType.toMap(), dayOrders.size)
}

private fun loadColor(level: LoadLevel, surface: Color): Color = when (level) {
    LoadLevel.FREE -> surface
    LoadLevel.LOW -> Color(0xFFC8E6C9)
    LoadLevel.MEDIUM -> Color(0xFFFFE082)
    LoadLevel.HIGH -> Color(0xFFFFAB91)
    LoadLevel.OVERLOAD -> Color(0xFFEF5350)
}

@Composable
fun OrdersCalendarDialog(
    orders: List<Order>,
    selectedDay: Long,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val today = LocalDate.now()
    val zone = ZoneId.systemDefault()
    val selectedDate = Instant.ofEpochMilli(selectedDay).atZone(zone).toLocalDate()
    var month by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    var pickedDate by remember { mutableStateOf(selectedDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Календарь загрузки") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MonthHeader(
                    month = month,
                    onPrev = { month = month.minusMonths(1) },
                    onNext = { month = month.plusMonths(1) }
                )
                WeekdayHeader()
                MonthGrid(
                    month = month,
                    today = today,
                    selected = pickedDate,
                    loadFor = { computeDayLoad(orders, it) },
                    onClick = { pickedDate = it }
                )
                Legend()
                DaySummary(orders, pickedDate)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSelect(pickedDate.atStartOfDay(zone).toInstant().toEpochMilli())
            }) { Text("Показать этот день") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        }
    )
}

@Composable
private fun MonthHeader(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    val name = month.month
        .getDisplayName(JavaTextStyle.FULL_STANDALONE, Locale("ru"))
        .replaceFirstChar { it.titlecase(Locale("ru")) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Предыдущий месяц")
        }
        Text(
            "$name ${month.year}",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Следующий месяц")
        }
    }
}

@Composable
private fun WeekdayHeader() {
    Row {
        listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { d ->
            Text(
                d,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    today: LocalDate,
    selected: LocalDate,
    loadFor: (LocalDate) -> DayLoad,
    onClick: (LocalDate) -> Unit
) {
    val firstOfMonth = month.atDay(1)
    val cellsBefore = firstOfMonth.dayOfWeek.value - 1 // Mon=1
    val gridStart = firstOfMonth.minusDays(cellsBefore.toLong())
    val surface = MaterialTheme.colorScheme.surface
    for (row in 0 until 6) {
        Row {
            for (col in 0 until 7) {
                val date = gridStart.plusDays((row * 7L + col))
                val inMonth = date.month == month.month
                val load = if (inMonth) loadFor(date) else DayLoad(0.0, 0.0, emptyMap(), 0)
                DayCell(
                    date = date,
                    inMonth = inMonth,
                    load = load,
                    isToday = date == today,
                    isSelected = date == selected,
                    surface = surface,
                    onClick = { onClick(date) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    inMonth: Boolean,
    load: DayLoad,
    isToday: Boolean,
    isSelected: Boolean,
    surface: Color,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val bg = if (inMonth) loadColor(load.level, surface) else surface
    val border = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.outline
        else -> Color.Transparent
    }
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(shape)
            .background(bg)
            .border(width = if (isSelected || isToday) 2.dp else 0.dp, color = border, shape = shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (inMonth) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            load.dominant?.let { type ->
                Icon(
                    type.icon,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
private fun Legend() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendDot(Color(0xFFC8E6C9), "Свободно")
        LegendDot(Color(0xFFFFE082), "Средне")
        LegendDot(Color(0xFFFFAB91), "Плотно")
        LegendDot(Color(0xFFEF5350), "Перегруз")
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Text(" $label", style = MaterialTheme.typography.labelSmall)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DaySummary(orders: List<Order>, date: LocalDate) {
    val load = computeDayLoad(orders, date)
    val month = date.month.getDisplayName(JavaTextStyle.FULL_STANDALONE, Locale("ru"))
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "${date.dayOfMonth} $month — ${formatHours(load.total)} ч (${load.orderCount} заказов)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        if (load.byType.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                load.byType.forEach { (type, hours) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            type.icon,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            " ${type.label}: ${formatHours(hours)} ч",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        when (load.level) {
            LoadLevel.OVERLOAD -> {
                val warn = if (load.noisy > NOISY_LIMIT) {
                    "Перегружено: шумных работ ${formatHours(load.noisy)} ч при норме " +
                        "${NOISY_LIMIT.toInt()} ч (с 13:00 до 15:00 — тихий час). " +
                        "Часть заявок лучше перенести."
                } else {
                    "Перегружено: всего ${formatHours(load.total)} ч при норме " +
                        "${OVERLOAD_HOURS.toInt()} ч. Часть заявок лучше перенести."
                }
                Text(warn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            LoadLevel.HIGH -> Text(
                "Плотная загрузка — времени почти не осталось.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            LoadLevel.MEDIUM -> Text(
                "Средняя загрузка.",
                style = MaterialTheme.typography.bodySmall
            )
            LoadLevel.LOW -> Text(
                "Свободно — можно добавить заявки.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            LoadLevel.FREE -> Text(
                "Заявок на этот день нет.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatHours(h: Double): String =
    if (h % 1.0 == 0.0) h.toInt().toString()
    else String.format(Locale.US, "%.1f", h)
