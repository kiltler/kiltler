package com.kiltler.assistant.ui.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kiltler.assistant.data.Reminder
import com.kiltler.assistant.ui.SectionHeader
import com.kiltler.assistant.data.Order
import com.kiltler.assistant.ui.VoiceTextField
import com.kiltler.assistant.ui.formatDateTime
import com.kiltler.assistant.ui.pickDateTime

@Composable
fun ScheduleScreen(
    reminders: List<Reminder>,
    orders: List<Order>,
    editing: Reminder?,
    showDialog: Boolean,
    onDismissDialog: () -> Unit,
    onSave: (Reminder) -> Unit,
    onToggleDone: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("finance", Context.MODE_PRIVATE) }
    var matPct by remember { mutableStateOf(prefs.getInt("mat", 30)) }
    var adsPct by remember { mutableStateOf(prefs.getInt("ads", 10)) }
    var showFinance by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(setOf<Long>()) }
    val toggleExpand: (Long) -> Unit = { id ->
        expanded = if (id in expanded) expanded - id else expanded + id
    }

    val now = System.currentTimeMillis()
    val active = reminders.filter { !it.isDone }
    val overdue = active.filter { it.timeMillis < now }
    val today = active.filter { it.timeMillis >= now && isToday(it.timeMillis) }
    val upcoming = active.filter { it.timeMillis >= now && !isToday(it.timeMillis) }
    val done = reminders.filter { it.isDone }

    Column(modifier = Modifier.fillMaxSize()) {
        FinanceCard(
            orders = orders,
            matPct = matPct,
            adsPct = adsPct,
            onClick = { showFinance = true },
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp)
        )
        if (reminders.isEmpty()) {
            Box(modifier = Modifier.weight(1f)) {
                EmptyState(
                    Icons.Default.Schedule,
                    "Пока нет напоминаний",
                    "Нажмите +, чтобы добавить первое"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                section("Просрочено", overdue, expanded, toggleExpand, onToggleDone, onDelete, accent = true)
                section("Сегодня", today, expanded, toggleExpand, onToggleDone, onDelete)
                section("Предстоящие", upcoming, expanded, toggleExpand, onToggleDone, onDelete)
                section("Выполнено", done, expanded, toggleExpand, onToggleDone, onDelete)
            }
        }
    }

    if (showDialog) {
        ReminderDialog(
            initial = editing,
            onDismiss = onDismissDialog,
            onSave = { onSave(it); onDismissDialog() }
        )
    }

    if (showFinance) {
        FinanceDialog(
            orders = orders,
            matPct = matPct,
            adsPct = adsPct,
            onDismiss = { showFinance = false },
            onSave = { m, a ->
                matPct = m
                adsPct = a
                prefs.edit().putInt("mat", m).putInt("ads", a).apply()
                showFinance = false
            }
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.section(
    title: String,
    data: List<Reminder>,
    expanded: Set<Long>,
    onToggleExpand: (Long) -> Unit,
    onToggleDone: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit,
    accent: Boolean = false
) {
    if (data.isEmpty()) return
    item(key = "header_$title") { SectionHeader(title) }
    items(data, key = { it.id }) { reminder ->
        ReminderRow(
            reminder = reminder,
            accent = accent,
            isExpanded = reminder.id in expanded,
            onToggleExpand = { onToggleExpand(reminder.id) },
            onToggleDone = onToggleDone,
            onDelete = onDelete
        )
    }
}

@Composable
private fun ReminderRow(
    reminder: Reminder,
    accent: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleDone: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (accent) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = reminder.isDone, onCheckedChange = { onToggleDone(reminder) })
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 10.dp)
                    .clickable(onClick = onToggleExpand)
            ) {
                Text(
                    reminder.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (reminder.isDone) TextDecoration.LineThrough else null
                )
                Text(
                    formatDateTime(reminder.timeMillis),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (reminder.description.isNotBlank()) {
                    Text(
                        reminder.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = { onDelete(reminder) }) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить")
            }
        }
    }
}

@Composable
private fun ReminderDialog(
    initial: Reminder?,
    onDismiss: () -> Unit,
    onSave: (Reminder) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var time by remember { mutableStateOf(initial?.timeMillis ?: defaultTime()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Новое напоминание" else "Напоминание") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                VoiceTextField(title, { title = it }, "Название", Modifier.fillMaxWidth())
                VoiceTextField(
                    description, { description = it }, "Описание",
                    Modifier.fillMaxWidth(), singleLine = false, minLines = 2
                )
                OutlinedButton(
                    onClick = { pickDateTime(context, time) { time = it } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Text("  ${formatDateTime(time)}")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            (initial ?: Reminder(title = "", timeMillis = time)).copy(
                                title = title.trim(),
                                description = description.trim(),
                                timeMillis = time,
                                isDone = false
                            )
                        )
                    }
                }
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

private fun defaultTime(): Long {
    val cal = java.util.Calendar.getInstance()
    cal.add(java.util.Calendar.HOUR_OF_DAY, 1)
    cal.set(java.util.Calendar.MINUTE, 0)
    cal.set(java.util.Calendar.SECOND, 0)
    return cal.timeInMillis
}

private fun isToday(millis: Long): Boolean {
    val a = java.util.Calendar.getInstance()
    val b = java.util.Calendar.getInstance().apply { timeInMillis = millis }
    return a.get(java.util.Calendar.YEAR) == b.get(java.util.Calendar.YEAR) &&
        a.get(java.util.Calendar.DAY_OF_YEAR) == b.get(java.util.Calendar.DAY_OF_YEAR)
}

@Composable
fun EmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon, contentDescription = null,
                        modifier = Modifier.size(46.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
