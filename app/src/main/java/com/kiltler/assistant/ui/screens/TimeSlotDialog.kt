package com.kiltler.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kiltler.assistant.data.Order
import com.kiltler.assistant.ui.formatTime
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

/**
 * Выбор времени слотами по 30 минут с 09:00 до 21:00.
 * Занятые слоты подсвечиваются красным с указанием заказа, свободные — зелёным.
 */
@Composable
fun TimeSlotDialog(
    date: LocalDate,
    otherOrders: List<Order>,
    onPick: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val zone = ZoneId.systemDefault()
    val dayStart = remember(date) {
        date.atStartOfDay(zone).toInstant().toEpochMilli()
    }
    val slotMinutes = remember {
        // 09:00 .. 21:00 шаг 30 мин → 25 слотов
        (0..24).map { 9 * 60 + it * 30 }
    }
    val month = date.month
        .getDisplayName(JavaTextStyle.FULL_STANDALONE, Locale("ru"))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Время выезда — ${date.dayOfMonth} $month") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(slotMinutes) { minutesOfDay ->
                    val slotMs = dayStart + minutesOfDay * 60_000L
                    val occupant = findConflict(otherOrders, slotMs, 1L)
                    SlotRow(slotMs, occupant) {
                        onPick(slotMs)
                        onDismiss()
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } }
    )
}

@Composable
private fun SlotRow(slotMs: Long, occupant: Order?, onClick: () -> Unit) {
    val bg = if (occupant != null) Color(0xFFFFCDD2) else Color(0xFFE8F5E9)
    val textDark = Color(0xFF1A1C1E)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            formatTime(slotMs),
            fontWeight = FontWeight.Bold,
            color = textDark,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.weight(1f))
        if (occupant != null) {
            val firstType = WorkType.parse(occupant.description).entries
                .firstOrNull()?.key?.label ?: "заказ"
            Text(
                "${occupant.clientName} • $firstType",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFB71C1C)
            )
        } else {
            Text(
                "свободно",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF2E7D32)
            )
        }
    }
}
