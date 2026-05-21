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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.kiltler.assistant.ui.formatTime
import com.kiltler.assistant.ui.geocodeAddress
import com.kiltler.assistant.ui.openYandexDrivingRoute
import com.kiltler.assistant.ui.pickDateTime
import com.kiltler.assistant.ui.pickTime
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Виды работ по заказу — каждая запоминает количество и стандартную цену. */
enum class WorkType(
    val label: String,
    val icon: ImageVector,
    /** Среднее время на одну единицу работы в часах. */
    val hours: Double,
    /** true — шумные работы (нельзя в «тихий час» 13:00–15:00). */
    val noisy: Boolean,
    /** Стандартная цена за единицу, ₽ (0 — без авторасчёта). */
    val price: Double
) {
    CLEANING("Чистка", Icons.Default.CleaningServices, 0.5, false, 3500.0),
    REFILL("Заправка", Icons.Default.Colorize, 0.5, false, 0.0),
    INSTALL("Установка", Icons.Default.Build, 2.0, true, 11500.0),
    PRE_INSTALL("Закладка", Icons.Default.Plumbing, 2.5, true, 0.0),
    DEMOUNT("Демонтаж", Icons.Default.Handyman, 0.5, true, 0.0),
    SALE("Продажа", Icons.Default.Sell, 0.0, false, 0.0);

    companion object {
        private val ITEM_PATTERN = Regex("""^(.+?)(?:\s+[x×]\s*(\d+))?\s*$""")

        /** Разбирает поле описания в карту «вид работы → количество». */
        fun parse(raw: String): Map<WorkType, Int> {
            val result = mutableMapOf<WorkType, Int>()
            raw.split(",").forEach { piece ->
                val trimmed = piece.trim()
                if (trimmed.isEmpty()) return@forEach
                val match = ITEM_PATTERN.matchEntire(trimmed) ?: return@forEach
                val label = match.groupValues[1].trim()
                val qty = match.groupValues[2].toIntOrNull() ?: 1
                val type = entries.firstOrNull { it.label == label } ?: return@forEach
                if (qty > 0) result[type] = qty
            }
            return result
        }

        /** Собирает строку для хранения в поле описания заказа. */
        fun join(items: Map<WorkType, Int>): String =
            entries
                .mapNotNull { type ->
                    val qty = items[type] ?: 0
                    if (qty > 0) type to qty else null
                }
                .joinToString(", ") { (type, qty) ->
                    if (qty <= 1) type.label else "${type.label} x$qty"
                }
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
    var dayFilter by remember { mutableStateOf<Long?>(startOfToday()) }
    var showCalendar by remember { mutableStateOf(false) }

    val dayFiltered = if (dayFilter == null) orders
    else orders.filter { matchesDay(it.scheduledMillis, dayFilter!!) }

    val visible = dayFiltered
        .filter { o ->
            if (filter != null) o.status == filter!!.name
            else OrderStatus.from(o.status) != OrderStatus.DONE
        }
        .sortedWith(compareBy(nullsLast<Long>()) { it.scheduledMillis })

    val allCount = dayFiltered.count { OrderStatus.from(it.status) != OrderStatus.DONE }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 16.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { showCalendar = true }) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Календарь загрузки")
            }
            Text(
                dayFilter?.let { dayLabel(it) } ?: "Все дни",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            if (dayFilter != null) {
                TextButton(onClick = { dayFilter = null }) { Text("Все дни") }
            } else {
                TextButton(onClick = { dayFilter = startOfToday() }) { Text("Сегодня") }
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = filter == null,
                    onClick = { filter = null },
                    label = { Text("Все ($allCount)") }
                )
            }
            items(OrderStatus.entries) { status ->
                val count = dayFiltered.count { it.status == status.name }
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
                    OrderCard(
                        order,
                        onClick = { onEdit(order) },
                        onDelete = { onDelete(order) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        OrderDialog(
            initial = editing,
            allOrders = orders,
            onDismiss = onDismissDialog,
            onSave = { onSave(it); onDismissDialog() }
        )
    }

    if (showCalendar) {
        OrdersCalendarDialog(
            orders = orders,
            selectedDay = dayFilter ?: startOfToday(),
            onSelect = { dayFilter = it; showCalendar = false },
            onDismiss = { showCalendar = false }
        )
    }
}

@Composable
private fun OrderCard(
    order: Order,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val status = OrderStatus.from(order.status)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
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
            if (order.address.isNotBlank()) {
                InfoLine(Icons.Default.Place, fullAddress(order))
            }
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
                    onClick = {
                        scope.launch {
                            val result = geocodeAddress(context, order.address)
                            if (result != null) {
                                openYandexDrivingRoute(
                                    context, result.latitude, result.longitude
                                )
                            } else {
                                Toast.makeText(
                                    context,
                                    "Адрес не найден — уточните адрес заказа",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null)
                    Text("  Маршрут на карте")
                }
            }
        }
    }
}

/** Перечень видов работ в карточке заказа — иконка, название и количество. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WorkTypeChips(items: Map<WorkType, Int>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(top = 6.dp)
    ) {
        WorkType.entries.forEach { type ->
            val qty = items[type] ?: return@forEach
            if (qty <= 0) return@forEach
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    type.icon, contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    if (qty > 1) " ${type.label} ×$qty" else " ${type.label}",
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

@Composable
private fun OrderDialog(
    initial: Order?,
    allOrders: List<Order>,
    onDismiss: () -> Unit,
    onSave: (Order) -> Unit
) {
    val context = LocalContext.current
    var client by remember { mutableStateOf(initial?.clientName ?: "") }
    var phone by remember { mutableStateOf((initial?.phone ?: "").ifBlank { PHONE_PREFIX }) }
    var address by remember { mutableStateOf(initial?.address ?: "") }
    var apartment by remember { mutableStateOf(initial?.apartment ?: "") }
    var entrance by remember { mutableStateOf(initial?.entrance ?: "") }
    var workItems by remember {
        mutableStateOf(WorkType.parse(initial?.description ?: ""))
    }
    var priceText by remember { mutableStateOf(initial?.price?.takeIf { it > 0 }?.let { formatMoney(it) } ?: "") }
    var lastAuto by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(OrderStatus.from(initial?.status ?: OrderStatus.NEW.name)) }
    var scheduled by remember { mutableStateOf(initial?.scheduledMillis) }
    var reminderEnabled by remember { mutableStateOf(initial?.reminderEnabled ?: true) }
    var pickingDate by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()

    // Авторасчёт суммы по выбранным работам, пока пользователь не правит сумму вручную.
    LaunchedEffect(workItems) {
        val auto = workItems.entries.sumOf { (t, q) -> t.price * q }
        val autoStr = if (auto > 0) formatMoney(auto) else ""
        if (priceText.isEmpty() || priceText == lastAuto) {
            priceText = autoStr
        }
        lastAuto = autoStr
    }

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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = apartment,
                        onValueChange = { apartment = it },
                        label = { Text("Квартира") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = entrance,
                        onValueChange = { entrance = it },
                        label = { Text("Подъезд") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                SectionHeader("Виды работ")
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    WorkType.entries.forEach { type ->
                        val qty = workItems[type] ?: 0
                        val selected = qty > 0
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    workItems = if (selected) workItems - type
                                    else workItems + (type to 1)
                                },
                                label = { Text(type.label) },
                                leadingIcon = {
                                    Icon(
                                        type.icon, contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                            if (selected) {
                                Spacer(Modifier.weight(1f))
                                IconButton(onClick = {
                                    workItems = if (qty <= 1) workItems - type
                                    else workItems + (type to qty - 1)
                                }) { Icon(Icons.Default.Remove, contentDescription = "Меньше") }
                                Text(
                                    "$qty",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.widthIn(min = 22.dp)
                                )
                                IconButton(onClick = {
                                    workItems = workItems + (type to qty + 1)
                                }) { Icon(Icons.Default.Add, contentDescription = "Больше") }
                            } else {
                                Spacer(Modifier.weight(1f))
                            }
                        }
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
                    onClick = { pickingDate = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Text("  " + (scheduled?.let { formatDateTime(it) } ?: "Дата выезда не задана"))
                }
                if (scheduled != null) {
                    val date = Instant.ofEpochMilli(scheduled!!)
                        .atZone(ZoneId.systemDefault()).toLocalDate()
                    val others = allOrders.filter { it.id != (initial?.id ?: -1L) }
                    val proposed = (initial ?: Order(clientName = "")).copy(
                        scheduledMillis = scheduled,
                        description = WorkType.join(workItems),
                        status = OrderStatus.NEW.name
                    )
                    val load = computeDayLoad(others + proposed, date)
                    Text(
                        "Загрузка дня: ${formatHours(load.total)} ч • ${load.orderCount} заказов",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val conflict = findConflict(others, scheduled!!, totalDurationMs(workItems))
                    if (conflict != null) {
                        val conflictType = WorkType.parse(conflict.description)
                            .entries.firstOrNull()?.key?.label ?: "заказ"
                        Text(
                            "⚠ Пересекается с заявкой «${conflict.clientName}» " +
                                "($conflictType в ${formatTime(conflict.scheduledMillis!!)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
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
                                apartment = apartment.trim(),
                                entrance = entrance.trim(),
                                description = WorkType.join(workItems),
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

    if (pickingDate) {
        OrdersCalendarDialog(
            orders = allOrders.filter { it.id != (initial?.id ?: -1L) },
            selectedDay = scheduled ?: System.currentTimeMillis(),
            title = "Выбор даты выезда",
            confirmLabel = "Выбрать",
            onSelect = { dayMs ->
                pickingDate = false
                val previous = Calendar.getInstance().apply {
                    timeInMillis = scheduled ?: System.currentTimeMillis()
                }
                val target = Calendar.getInstance().apply {
                    timeInMillis = dayMs
                    set(Calendar.HOUR_OF_DAY, previous.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, previous.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                pickTime(context, target.timeInMillis) { picked ->
                    scheduled = picked
                }
            },
            onDismiss = { pickingDate = false }
        )
    }
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

/** Полный адрес заказа: улица плюс квартира и подъезд, если заданы. */
private fun fullAddress(order: Order): String = buildString {
    append(order.address)
    if (order.apartment.isNotBlank()) append(", кв. ${order.apartment}")
    if (order.entrance.isNotBlank()) append(", подъезд ${order.entrance}")
}

private fun startOfToday(): Long {
    val c = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return c.timeInMillis
}

private fun matchesDay(scheduledMillis: Long?, dayStart: Long): Boolean {
    if (scheduledMillis == null) return false
    val dayEnd = dayStart + 24L * 60 * 60 * 1000
    return scheduledMillis in dayStart until dayEnd
}

private val dayLabelFmt = SimpleDateFormat("d MMMM", Locale("ru"))

private fun dayLabel(dayStart: Long): String {
    val now = Calendar.getInstance()
    val day = Calendar.getInstance().apply { timeInMillis = dayStart }
    val isToday = now.get(Calendar.YEAR) == day.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR)
    val date = dayLabelFmt.format(Date(dayStart))
    return if (isToday) "Сегодня, $date" else date
}
