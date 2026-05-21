package com.kiltler.assistant.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.kiltler.assistant.data.Expense
import com.kiltler.assistant.data.Material
import com.kiltler.assistant.data.Order
import com.kiltler.assistant.data.OrderStatus
import com.kiltler.assistant.data.Reminder
import com.kiltler.assistant.data.WorkPlace
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Сериализация данных в JSON-файл и обратно. */
object BackupManager {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    fun toJson(data: BackupData): String = gson.toJson(data)

    /**
     * Толерантный парсер: читает поля вручную и подставляет defaults для
     * всего, чего нет в источнике. Так старые снимки (без `acModel`,
     * `acMargin`, `expenses`, `workPlaces` и т.п.) импортируются без падений
     * на Kotlin non-null проверках — Gson иначе создаёт сущность через
     * Unsafe в обход конструктора и оставляет недостающие поля `null`.
     */
    fun fromJson(json: String): BackupData {
        val root = JsonParser.parseString(json).asJsonObject
        return BackupData(
            version = root.intOr("version", 1),
            exportedAt = root.longOr("exportedAt", System.currentTimeMillis()),
            reminders = root.arrayOr("reminders").mapObjects(::parseReminder),
            orders = root.arrayOr("orders").mapObjects(::parseOrder),
            materials = root.arrayOr("materials").mapObjects(::parseMaterial),
            workPlaces = root.arrayOr("workPlaces").mapObjects(::parseWorkPlace),
            expenses = root.arrayOr("expenses").mapObjects(::parseExpense),
        )
    }

    private fun parseReminder(o: JsonObject) = Reminder(
        id = o.longOr("id", 0L),
        title = o.stringOr("title", ""),
        description = o.stringOr("description", ""),
        timeMillis = o.longOr("timeMillis", 0L),
        isDone = o.boolOr("isDone", false),
        createdAt = o.longOr("createdAt", System.currentTimeMillis()),
    )

    private fun parseOrder(o: JsonObject) = Order(
        id = o.longOr("id", 0L),
        clientName = o.stringOr("clientName", ""),
        phone = o.stringOr("phone", ""),
        address = o.stringOr("address", ""),
        apartment = o.stringOr("apartment", ""),
        entrance = o.stringOr("entrance", ""),
        description = o.stringOr("description", ""),
        price = o.doubleOr("price", 0.0),
        acModel = o.stringOr("acModel", ""),
        acMargin = o.doubleOr("acMargin", 0.0),
        status = o.stringOr("status", OrderStatus.NEW.name),
        scheduledMillis = o.longOrNull("scheduledMillis"),
        reminderEnabled = o.boolOr("reminderEnabled", true),
        createdAt = o.longOr("createdAt", System.currentTimeMillis()),
    )

    private fun parseMaterial(o: JsonObject) = Material(
        id = o.longOr("id", 0L),
        name = o.stringOr("name", ""),
        unit = o.stringOr("unit", "шт"),
        quantity = o.doubleOr("quantity", 0.0),
        minQuantity = o.doubleOr("minQuantity", 0.0),
        note = o.stringOr("note", ""),
    )

    private fun parseWorkPlace(o: JsonObject) = WorkPlace(
        id = o.longOr("id", 0L),
        title = o.stringOr("title", ""),
        description = o.stringOr("description", ""),
        latitude = o.doubleOr("latitude", 0.0),
        longitude = o.doubleOr("longitude", 0.0),
        createdAt = o.longOr("createdAt", System.currentTimeMillis()),
    )

    private fun parseExpense(o: JsonObject) = Expense(
        id = o.longOr("id", 0L),
        amount = o.doubleOr("amount", 0.0),
        note = o.stringOr("note", ""),
        createdAtMillis = o.longOr("createdAtMillis", System.currentTimeMillis()),
    )

    /** Сохраняет бэкап во временный файл и возвращает Uri для шаринга. */
    fun writeShareFile(context: Context, data: BackupData): Uri {
        val dir = File(context.cacheDir, "backups").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.getDefault()).format(Date())
        val file = File(dir, "assistant_backup_$stamp.json")
        file.writeText(toJson(data))
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun shareIntent(uri: Uri): Intent =
        Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Резервная копия — Ассистент")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

    fun readJson(context: Context, uri: Uri): String =
        context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.readBytes().toString(Charsets.UTF_8)
        } ?: error("Не удалось прочитать файл")
}

private fun JsonObject.present(name: String): Boolean = has(name) && !get(name).isJsonNull

private fun JsonObject.stringOr(name: String, default: String): String =
    if (present(name)) runCatching { get(name).asString }.getOrDefault(default) else default

private fun JsonObject.intOr(name: String, default: Int): Int =
    if (present(name)) runCatching { get(name).asInt }.getOrDefault(default) else default

private fun JsonObject.longOr(name: String, default: Long): Long =
    if (present(name)) runCatching { get(name).asLong }.getOrDefault(default) else default

private fun JsonObject.longOrNull(name: String): Long? =
    if (present(name)) runCatching { get(name).asLong }.getOrNull() else null

private fun JsonObject.doubleOr(name: String, default: Double): Double =
    if (present(name)) runCatching { get(name).asDouble }.getOrDefault(default) else default

private fun JsonObject.boolOr(name: String, default: Boolean): Boolean =
    if (present(name)) runCatching { get(name).asBoolean }.getOrDefault(default) else default

private fun JsonObject.arrayOr(name: String): JsonArray =
    if (present(name) && get(name).isJsonArray) getAsJsonArray(name) else JsonArray()

private inline fun <T> JsonArray.mapObjects(transform: (JsonObject) -> T): List<T> =
    mapNotNull { el: JsonElement -> if (el.isJsonObject) transform(el.asJsonObject) else null }
