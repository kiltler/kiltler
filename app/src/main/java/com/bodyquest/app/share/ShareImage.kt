package com.bodyquest.app.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/** Снимок прогресса для «поделиться». */
data class ShareSnapshot(
    val name: String,
    val rankTitle: String,
    val level: Int,
    val streak: Int,
    val weightKg: Double,
    val waistCm: Double,
    val unlocked: Int,
    val totalAch: Int,
)

/** Рисует карточку прогресса в Bitmap и открывает системный «Поделиться». Без сети. */
object ShareImage {

    fun share(context: Context, s: ShareSnapshot) {
        val bmp = render(s)
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(dir, "bodyquest_progress.png")
        FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Качаю героя в BodyQuest — ур. ${s.level}, ${s.rankTitle}!")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(send, "Поделиться прогрессом").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun render(s: ShareSnapshot): Bitmap {
        val size = 1080
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val cv = Canvas(bmp)

        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, size.toFloat(),
                0xFF1C2438.toInt(), 0xFF0B0E14.toInt(), Shader.TileMode.CLAMP,
            )
        }
        cv.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bg)

        // Акцентная полоса слева
        val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF7C5CFF.toInt() }
        cv.drawRect(0f, 0f, 18f, size.toFloat(), accent)

        val bold = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val gold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFB454.toInt(); textSize = 86f; typeface = bold
        }
        cv.drawText("BODYQUEST", 80f, 180f, gold)

        val white = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE6E9F2.toInt(); textSize = 56f; typeface = bold
        }
        cv.drawText("${s.rankTitle}  ·  ур. ${s.level}", 80f, 280f, white)

        val label = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF9AA3BF.toInt(); textSize = 40f }
        cv.drawText(s.name, 80f, 340f, label)

        val stat = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF22D3A6.toInt(); textSize = 64f; typeface = bold }
        var y = 520f
        val lines = listOf(
            "Серия: ${s.streak} дн.",
            "Вес: ${s.weightKg} кг",
            "Талия: ${s.waistCm} см",
            "Достижения: ${s.unlocked}/${s.totalAch}",
        )
        lines.forEach { cv.drawText(it, 80f, y, stat); y += 110f }

        val footer = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF9AA3BF.toInt(); textSize = 38f }
        cv.drawText("Путь от новобранца к легенде", 80f, (size - 70).toFloat(), footer)
        return bmp
    }
}
