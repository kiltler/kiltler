package com.kiltler.assistant.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kiltler.assistant.data.Material
import com.kiltler.assistant.ui.VoiceTextField

@Composable
fun MaterialsScreen(
    materials: List<Material>,
    editing: Material?,
    showDialog: Boolean,
    onDismissDialog: () -> Unit,
    onSave: (Material) -> Unit,
    onDelete: (Material) -> Unit,
    onEdit: (Material) -> Unit,
    onAdjust: (Material, Double) -> Unit
) {
    val low = materials.filter { it.isLow }

    if (materials.isEmpty()) {
        EmptyState(Icons.Default.Inventory2, "Склад пуст", "Нажмите +, чтобы добавить материал")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (low.isNotEmpty()) {
                item(key = "low_banner") {
                    LowStockBanner(low.size)
                }
            }
            items(materials, key = { it.id }) { material ->
                MaterialCard(
                    material,
                    onClick = { onEdit(material) },
                    onDelete = { onDelete(material) },
                    onPlus = { onAdjust(material, 1.0) },
                    onMinus = { onAdjust(material, -1.0) }
                )
            }
        }
    }

    if (showDialog) {
        MaterialDialog(
            initial = editing,
            onDismiss = onDismissDialog,
            onSave = { onSave(it); onDismissDialog() }
        )
    }
}

@Composable
private fun LowStockBanner(count: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Text(
                "Заканчивается материалов: $count",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MaterialCard(
    material: Material,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onPlus: () -> Unit,
    onMinus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (material.isLow) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onClick)
            ) {
                Text(
                    material.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Остаток: ${formatNum(material.quantity)} ${material.unit}" +
                        "  •  мин. ${formatNum(material.minQuantity)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (material.isLow) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (material.note.isNotBlank()) {
                    Text(
                        material.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            FilledTonalIconButton(onClick = onMinus) {
                Icon(Icons.Default.Remove, contentDescription = "Минус")
            }
            Text(
                formatNum(material.quantity),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
            FilledTonalIconButton(onClick = onPlus) {
                Icon(Icons.Default.Add, contentDescription = "Плюс")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить")
            }
        }
    }
}

@Composable
private fun MaterialDialog(
    initial: Material?,
    onDismiss: () -> Unit,
    onSave: (Material) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var unit by remember { mutableStateOf(initial?.unit ?: "шт") }
    var quantity by remember { mutableStateOf(initial?.quantity?.let { formatNum(it) } ?: "0") }
    var minQuantity by remember { mutableStateOf(initial?.minQuantity?.let { formatNum(it) } ?: "0") }
    var note by remember { mutableStateOf(initial?.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Новый материал" else "Материал") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                VoiceTextField(name, { name = it }, "Название", Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                        label = { Text("Остаток") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Ед.") },
                        singleLine = true,
                        modifier = Modifier.weight(0.7f)
                    )
                }
                OutlinedTextField(
                    value = minQuantity,
                    onValueChange = { minQuantity = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                    label = { Text("Мин. остаток") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                VoiceTextField(note, { note = it }, "Заметка", Modifier.fillMaxWidth(), singleLine = false)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            (initial ?: Material(name = "")).copy(
                                name = name.trim(),
                                unit = unit.trim().ifBlank { "шт" },
                                quantity = quantity.replace(',', '.').toDoubleOrNull() ?: 0.0,
                                minQuantity = minQuantity.replace(',', '.').toDoubleOrNull() ?: 0.0,
                                note = note.trim()
                            )
                        )
                    }
                }
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

private fun formatNum(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
