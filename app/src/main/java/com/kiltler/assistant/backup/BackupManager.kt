package com.kiltler.assistant.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Сериализация данных в JSON-файл и обратно. */
object BackupManager {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    fun toJson(data: BackupData): String = gson.toJson(data)

    fun fromJson(json: String): BackupData = gson.fromJson(json, BackupData::class.java)

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
