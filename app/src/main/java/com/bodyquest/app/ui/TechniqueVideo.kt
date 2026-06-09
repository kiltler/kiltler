package com.bodyquest.app.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

/**
 * Открывает видео с техникой упражнения во внешнем приложении (YouTube/браузер).
 * Само приложение остаётся офлайн — интернет нужен только в момент просмотра.
 */
fun openTechniqueVideo(context: Context, exerciseName: String) {
    val query = URLEncoder.encode("$exerciseName техника выполнения", "UTF-8")
    val url = "https://www.youtube.com/results?search_query=$query"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "Не найден браузер для открытия видео", Toast.LENGTH_SHORT).show()
    }
}
