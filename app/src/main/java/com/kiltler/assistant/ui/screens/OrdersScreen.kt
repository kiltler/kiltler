package com.kiltler.assistant.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.kiltler.assistant.data.Order
import com.kiltler.assistant.data.OrderStatus
import com.kiltler.assistant.ui.SectionHeader
import com.kiltler.assistant.ui.StatusBadge
import com.kiltler.assistant.ui.VoiceTextField
import com.kiltler.assistant.ui.formatDateTime
import com.kiltler.assistant.ui.pickDateTime
import com.kiltler.assistant.ui.qualifyAddress

/** Виды работ по заказу — можно выбрать несколько одновременно. */
enum class WorkType(val label: String, val icon: ImageVector) {
    CLEANING("Чистка", Icons.Default.CleaningServices),
    REFILL("Заправка", Icons.Default.Colorize),
    INSTALL("Установка", Icons.Default.Build),
    PRE_INSTALL("Закладка", Icons.Default.Plumbing),
    SALE("Продажа", Icons.Default.Sell);

    companion object {
        /** Разбирает поле описания заказа в список выбранных видов работ. */
        fun parse(raw: String): List<WorkType> {
            val parts = raw.split(",").map { it.trim() }
            return entries.filter { it.label in parts }
        }

        /** Собирает строку для хранения в поле описания заказа. */
        fun join(types: Collection<WorkType>): String =
            entries.filter { it in types }.joinToString(", ") { it.label }
    }
}

@Composable
fun OrdersScreen(
    orders: List<Order>,
    editing: Order?,
    showDialog: Boolean,
    onDismissDialog: () -> Unit,
    onSave: (Order) -> Unit,
    onDelete: (Order) -> Unit,
    onEdit: (Order) -> Unit
) {
    var filter by remember { mutableStateOf<OrderStatus?>(null) }
    val visible = orders.filter { filter == null || it.status == filter!!.name }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = filter == null,
                    onClick = { filter = null },
                    label = { Text("Все (${orders.size})") }
                )
            }
            items(OrderStatus.entries) { status ->
                val count = orders.count { it.status == status.name }
                FilterChip(
                    selected = filter == status,
                    onClick = { filter = if (filter == status) null else status },
                    label = { Text("${status.label} ($count)") }
                )
            }
        }

        if (visible.isEmpty()) {
            EmptyState(Icons.Default.WorkOutline, "Нет заказов", "Нажмите +, чтобы добавить заказ")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(visible, key = { it.id }) { order ->
                    OrderCard(order, onClick = { onEdit(order) }, onDelete = { onDelete(order) })
                }
            }
        }
    }

    if (showDialog) {
        OrderDialog(
            initial = editing,
            onDismiss = onDismissDialog,
            onSave = { onSave(it); onDismissDialog() }
        )
    }
}

@Composable
private fun OrderCard(order: Order, onClick: () -> Unit, onDelete: () -> Unit) {
    val status = OrderStatus.from(order.status)
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    order.clientName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(status.label, status.color)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить")
                }
            }
            if (order.phone.isNotBlank()) InfoLine(Icons.Default.Call, order.phone)
            if (order.address.isNotBlank()) InfoLine(Icons.Default.Place, order.address)
            if (order.scheduledMillis != null) {
                InfoLine(Icons.Default.Schedule, formatDateTime(order.scheduledMillis))
            }

            val workTypes = WorkType.parse(order.description)
            if (workTypes.isNotEmpty()) {
                WorkTypeChips(workTypes)
            } else if (order.description.isNotBlank()) {
                Text(
                    order.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (order.price > 0) {
                Text(
                    "Сумма: ${formatMoney(order.price)} ₽",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (order.phone.isNotBlank()) {
                FilledTonalButton(
                    onClick = { dialPhone(context, order.phone) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null)
                    Text("  Позвонить")
                }
            }
            if (order.address.isNotBlank()) {
                FilledTonalButton(
                    onClick = { routeToAddress(context, order.address) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null)
                    Text("  Маршрут")
                }
            }
        }
    }
}

/** Перечень видов работ в карточке заказа — иконка плюс название. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WorkTypeChips(types: List<WorkType>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(top = 6.dp)
    ) {
        types.forEach { type ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    type.icon, contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    " ${type.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Открывает номеронабиратель с заранее введённым номером заказа. */
private fun dialPhone(context: Context, phone: String) {
    try {
        context.startActivity(
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.trim()}"))
        )
    } catch (e: Exception) {
        Toast.makeText(context, "Не удалось открыть набор номера", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Строит маршрут до адреса заказа в Яндекс Картах. Адрес передаётся
 * текстом (с уточнением города) — Яндекс сам определяет координаты,
 * это точнее системного геокодера. Навигатор намеренно не вызывается.
 */
private fun routeToAddress(context: Context, address: String) {
    val encoded = Uri.encode(qualifyAddress(address))
    val targets = listOf(
        "yandexmaps://maps.yandex.ru/?rtext=~$encoded&rtt=auto",
        "https://yandex.ru/maps/?rtext=~$encoded&rtt=auto"
    )
    for (uri in targets) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            return
        } catch (e: Exception) {
            // приложение не найдено — пробуем следующий вариант
        }
    }
    Toast.makeText(context, "Не удалось открыть Яндекс Карты", Toast.LENGTH_SHORT).show()
}

@Composable
private fun InfoLine(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 3.dp)
    ) {
        Icon(
            icon, contentDescription = null,
            modifier = Modifier.padding(end = 6.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OrderDialog(
    initial: Order?,
    onDismiss: () -> Unit,
    onSave: (Order) -> Unit
) {
    val context = LocalContext.current
    var client by remember { mutableStateOf(initial?.clientName ?: "") }
    var phone by remember { mutableStateOf((initial?.phone ?: "").ifBlank { PHONE_PREFIX }) }
    var address by remember { mutableStateOf(initial?.address ?: "") }
    var workTypes by remember {
        mutableStateOf(WorkType.parse(initial?.description ?: "").toSet())
    }
    var priceText by remember { mutableStateOf(initial?.price?.takeIf { it > 0 }?.let { formatMoney(it) } ?: "") }
    var status by remember { mutableStateOf(OrderStatus.from(initial?.status ?: OrderStatus.NEW.name)) }
    var scheduled by remember { mutableStateOf(initial?.scheduledMillis) }
    var reminderEnabled by remember { mutableStateOf(initial?.reminderEnabled ?: true) }
    val scroll = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Новый заказ" else "Заказ") },
        text = {
            Column(
                modifier = Modifier.androidVerticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VoiceTextField(client, { client = it }, "Клиент", Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = formatPhone(it) },
                    label = { Text("Телефон") },
                    supportingText = { Text("Формат: +7 и 10 цифр") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                VoiceTextField(address, { address = it }, "Адрес", Modifier.fillMaxWidth())

                SectionHeader("Виды работ")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WorkType.entries.forEach { type ->
                        FilterChip(
                            selected = type in workTypes,
                            onClick = {
                                workTypes = if (type in workTypes) workTypes - type
                                else workTypes + type
                            },
                            label = { Text(type.label) },
                            leadingIcon = {
                                Icon(
                                    type.icon, contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                    label = { Text("Сумма, ₽") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                SectionHeader("Статус")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(OrderStatus.entries) { s ->
                        FilterChip(
                            selected = status == s,
                            onClick = { status = s },
                            label = { Text(s.label) }
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        pickDateTime(context, scheduled ?: System.currentTimeMillis()) {
                            scheduled = it
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Text("  " + (scheduled?.let { formatDateTime(it) } ?: "Дата выезда не задана"))
                }
                if (scheduled != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                        Text("  Напомнить о заказе", style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = { scheduled = null }) { Text("Убрать дату") }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (client.isNotBlank()) {
                        val price = priceText.replace(',', '.').replace(" ", "").toDoubleOrNull() ?: 0.0
                        val phoneClean = phone
                            .takeIf { it.removePrefix(PHONE_PREFIX).any(Char::isDigit) }
                            ?.trim() ?: ""
                        onSave(
                            (initial ?: Order(clientName = "")).copy(
                                clientName = client.trim(),
                                phone = phoneClean,
                                address = address.trim(),
                                description = WorkType.join(workTypes),
                                price = price,
                                status = status.name,
                                scheduledMillis = scheduled,
                                reminderEnabled = reminderEnabled
                            )
                        )
                    }
                }
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

private fun Modifier.androidVerticalScroll(state: androidx.compose.foundation.ScrollState): Modifier =
    this.verticalScroll(state)

private const val PHONE_PREFIX = "+7"

/** Удерживает префикс +7 и оставляет под ввод не более 10 цифр. */
private fun formatPhone(input: String): String {
    var digits = input.removePrefix(PHONE_PREFIX).filter { it.isDigit() }
    if (digits.length > 10 && (digits.startsWith("7") || digits.startsWith("8"))) {
        digits = digits.drop(1)
    }
    return PHONE_PREFIX + digits.take(10)
}

private fun formatMoney(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
