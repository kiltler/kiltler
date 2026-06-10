package com.bodyquest.app.ui.art

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

/** Возвращает id ресурса drawable по имени или 0, если файла нет. */
@Composable
fun rememberDrawableId(name: String): Int {
    val context = LocalContext.current
    return remember(name) {
        context.resources.getIdentifier(name, "drawable", context.packageName)
    }
}

/**
 * Если в res/drawable есть файл [name] — показываем картинку, иначе — [fallback].
 * Позволяет добавлять готовый арт (боссы, ачивки), просто кладя файлы в drawable.
 */
@Composable
fun AssetImageOr(
    name: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    fallback: @Composable () -> Unit,
) {
    val id = rememberDrawableId(name)
    if (id != 0) {
        Image(
            painter = painterResource(id),
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier,
        )
    } else {
        fallback()
    }
}

/**
 * Фигура ранга: картинка `rank_<index>` из drawable, либо нарисованный силуэт (фолбэк).
 */
@Composable
fun RankArt(
    rankIndex: Int,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFE6E9F2),
    accent: Color = Color(0xFFFFB454),
) {
    val id = rememberDrawableId("rank_$rankIndex")
    if (id != 0) {
        Image(
            painter = painterResource(id),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = modifier,
        )
    } else {
        RankFigure(rankIndex, modifier, color, accent)
    }
}
