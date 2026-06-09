package com.bodyquest.app.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

/** Тип позы для схематичной иллюстрации техники. */
enum class ExercisePose {
    PUSHUP, PLANK, PULLUP, DIP, SQUAT, HINGE, PRESS, CURL, LATERAL, ROW,
    FACEPULL, HANG_LEG_RAISE, STRETCH, GETUP, GENERIC
}

private typealias Seg = Pair<Offset, Offset>

private class PoseDef(
    val head: Offset,
    val headR: Float,
    val bones: List<Seg>,
    val bars: List<Seg> = emptyList(),
    val weights: List<Offset> = emptyList(),
)

private fun o(x: Float, y: Float) = Offset(x, y)

/** Сопоставление упражнения и позы для иллюстрации. */
fun poseFor(exerciseId: String): ExercisePose = when (exerciseId) {
    "pullup" -> ExercisePose.PULLUP
    "band_row", "db_rear_fly" -> ExercisePose.ROW
    "face_pull" -> ExercisePose.FACEPULL
    "rear_delt_band", "db_lateral" -> ExercisePose.LATERAL
    "dips" -> ExercisePose.DIP
    "pushup" -> ExercisePose.PUSHUP
    "kb_press", "kb_snatch" -> ExercisePose.PRESS
    "db_curl" -> ExercisePose.CURL
    "kb_swing", "kb_clean" -> ExercisePose.HINGE
    "kb_goblet_squat" -> ExercisePose.SQUAT
    "kb_tgu" -> ExercisePose.GETUP
    "hanging_leg_raise" -> ExercisePose.HANG_LEG_RAISE
    "plank" -> ExercisePose.PLANK
    "mobility_flow", "band_shoulder_dislocate", "hip_stretch" -> ExercisePose.STRETCH
    else -> ExercisePose.GENERIC
}

private fun poseDef(pose: ExercisePose): PoseDef = when (pose) {
    ExercisePose.PUSHUP -> PoseDef(
        head = o(0.14f, 0.40f), headR = 0.075f,
        bones = listOf(
            o(0.20f, 0.43f) to o(0.55f, 0.55f),
            o(0.55f, 0.55f) to o(0.80f, 0.66f),
            o(0.80f, 0.66f) to o(0.95f, 0.74f),
            o(0.22f, 0.44f) to o(0.24f, 0.80f),
        ),
    )
    ExercisePose.PLANK -> PoseDef(
        head = o(0.14f, 0.46f), headR = 0.07f,
        bones = listOf(
            o(0.20f, 0.48f) to o(0.58f, 0.55f),
            o(0.58f, 0.55f) to o(0.80f, 0.62f),
            o(0.80f, 0.62f) to o(0.95f, 0.70f),
            o(0.22f, 0.48f) to o(0.22f, 0.66f),
            o(0.22f, 0.66f) to o(0.34f, 0.78f),
        ),
    )
    ExercisePose.PULLUP -> PoseDef(
        head = o(0.5f, 0.34f), headR = 0.075f,
        bars = listOf(o(0.28f, 0.12f) to o(0.72f, 0.12f)),
        bones = listOf(
            o(0.5f, 0.40f) to o(0.5f, 0.64f),
            o(0.45f, 0.41f) to o(0.41f, 0.13f),
            o(0.55f, 0.41f) to o(0.59f, 0.13f),
            o(0.48f, 0.64f) to o(0.44f, 0.82f),
            o(0.52f, 0.64f) to o(0.56f, 0.82f),
        ),
    )
    ExercisePose.DIP -> PoseDef(
        head = o(0.5f, 0.24f), headR = 0.075f,
        bars = listOf(o(0.20f, 0.52f) to o(0.34f, 0.52f), o(0.66f, 0.52f) to o(0.80f, 0.52f)),
        bones = listOf(
            o(0.5f, 0.30f) to o(0.5f, 0.60f),
            o(0.45f, 0.31f) to o(0.30f, 0.52f),
            o(0.55f, 0.31f) to o(0.70f, 0.52f),
            o(0.5f, 0.60f) to o(0.42f, 0.74f),
            o(0.42f, 0.74f) to o(0.50f, 0.82f),
            o(0.5f, 0.60f) to o(0.58f, 0.74f),
            o(0.58f, 0.74f) to o(0.66f, 0.82f),
        ),
    )
    ExercisePose.SQUAT -> PoseDef(
        head = o(0.5f, 0.16f), headR = 0.07f,
        bones = listOf(
            o(0.5f, 0.22f) to o(0.5f, 0.52f),
            o(0.5f, 0.26f) to o(0.42f, 0.42f),
            o(0.5f, 0.26f) to o(0.58f, 0.42f),
            o(0.5f, 0.52f) to o(0.36f, 0.60f),
            o(0.36f, 0.60f) to o(0.36f, 0.84f),
            o(0.5f, 0.52f) to o(0.64f, 0.60f),
            o(0.64f, 0.60f) to o(0.64f, 0.84f),
        ),
        weights = listOf(o(0.5f, 0.42f)),
    )
    ExercisePose.HINGE -> PoseDef(
        head = o(0.30f, 0.30f), headR = 0.07f,
        bones = listOf(
            o(0.36f, 0.34f) to o(0.62f, 0.50f),
            o(0.38f, 0.35f) to o(0.46f, 0.64f),
            o(0.62f, 0.50f) to o(0.66f, 0.66f),
            o(0.66f, 0.66f) to o(0.70f, 0.88f),
            o(0.62f, 0.50f) to o(0.55f, 0.66f),
            o(0.55f, 0.66f) to o(0.55f, 0.88f),
        ),
        weights = listOf(o(0.46f, 0.66f)),
    )
    ExercisePose.PRESS -> PoseDef(
        head = o(0.5f, 0.20f), headR = 0.07f,
        bones = listOf(
            o(0.5f, 0.26f) to o(0.5f, 0.58f),
            o(0.5f, 0.28f) to o(0.40f, 0.16f),
            o(0.40f, 0.16f) to o(0.42f, 0.06f),
            o(0.5f, 0.28f) to o(0.60f, 0.16f),
            o(0.60f, 0.16f) to o(0.58f, 0.06f),
            o(0.5f, 0.58f) to o(0.44f, 0.92f),
            o(0.5f, 0.58f) to o(0.56f, 0.92f),
        ),
        weights = listOf(o(0.42f, 0.06f), o(0.58f, 0.06f)),
    )
    ExercisePose.CURL -> PoseDef(
        head = o(0.5f, 0.17f), headR = 0.07f,
        bones = listOf(
            o(0.5f, 0.23f) to o(0.5f, 0.58f),
            o(0.46f, 0.25f) to o(0.44f, 0.45f),
            o(0.44f, 0.45f) to o(0.40f, 0.34f),
            o(0.54f, 0.25f) to o(0.56f, 0.45f),
            o(0.56f, 0.45f) to o(0.60f, 0.34f),
            o(0.5f, 0.58f) to o(0.45f, 0.92f),
            o(0.5f, 0.58f) to o(0.55f, 0.92f),
        ),
        weights = listOf(o(0.40f, 0.34f), o(0.60f, 0.34f)),
    )
    ExercisePose.LATERAL -> PoseDef(
        head = o(0.5f, 0.17f), headR = 0.07f,
        bones = listOf(
            o(0.5f, 0.23f) to o(0.5f, 0.58f),
            o(0.46f, 0.30f) to o(0.18f, 0.34f),
            o(0.54f, 0.30f) to o(0.82f, 0.34f),
            o(0.5f, 0.58f) to o(0.45f, 0.92f),
            o(0.5f, 0.58f) to o(0.55f, 0.92f),
        ),
        weights = listOf(o(0.18f, 0.34f), o(0.82f, 0.34f)),
    )
    ExercisePose.ROW -> PoseDef(
        head = o(0.28f, 0.32f), headR = 0.07f,
        bones = listOf(
            o(0.34f, 0.36f) to o(0.66f, 0.52f),
            o(0.37f, 0.37f) to o(0.42f, 0.50f),
            o(0.42f, 0.50f) to o(0.42f, 0.40f),
            o(0.66f, 0.52f) to o(0.70f, 0.70f),
            o(0.70f, 0.70f) to o(0.74f, 0.90f),
            o(0.66f, 0.52f) to o(0.58f, 0.70f),
            o(0.58f, 0.70f) to o(0.56f, 0.90f),
        ),
        weights = listOf(o(0.42f, 0.40f)),
    )
    ExercisePose.FACEPULL -> PoseDef(
        head = o(0.5f, 0.20f), headR = 0.07f,
        bars = listOf(o(0.44f, 0.22f) to o(0.5f, 0.10f), o(0.56f, 0.22f) to o(0.5f, 0.10f)),
        bones = listOf(
            o(0.5f, 0.26f) to o(0.5f, 0.58f),
            o(0.46f, 0.28f) to o(0.32f, 0.30f),
            o(0.32f, 0.30f) to o(0.44f, 0.22f),
            o(0.54f, 0.28f) to o(0.68f, 0.30f),
            o(0.68f, 0.30f) to o(0.56f, 0.22f),
            o(0.5f, 0.58f) to o(0.45f, 0.92f),
            o(0.5f, 0.58f) to o(0.55f, 0.92f),
        ),
    )
    ExercisePose.HANG_LEG_RAISE -> PoseDef(
        head = o(0.42f, 0.30f), headR = 0.07f,
        bars = listOf(o(0.28f, 0.10f) to o(0.72f, 0.10f)),
        bones = listOf(
            o(0.42f, 0.36f) to o(0.50f, 0.54f),
            o(0.40f, 0.32f) to o(0.38f, 0.11f),
            o(0.46f, 0.32f) to o(0.50f, 0.11f),
            o(0.50f, 0.54f) to o(0.66f, 0.52f),
            o(0.66f, 0.52f) to o(0.84f, 0.50f),
        ),
    )
    ExercisePose.STRETCH -> PoseDef(
        head = o(0.34f, 0.24f), headR = 0.07f,
        bones = listOf(
            o(0.40f, 0.30f) to o(0.54f, 0.52f),
            o(0.42f, 0.31f) to o(0.40f, 0.12f),
            o(0.54f, 0.52f) to o(0.40f, 0.66f),
            o(0.40f, 0.66f) to o(0.36f, 0.86f),
            o(0.54f, 0.52f) to o(0.74f, 0.66f),
            o(0.74f, 0.66f) to o(0.90f, 0.84f),
        ),
    )
    ExercisePose.GETUP -> PoseDef(
        head = o(0.30f, 0.62f), headR = 0.07f,
        bones = listOf(
            o(0.36f, 0.62f) to o(0.62f, 0.66f),
            o(0.40f, 0.60f) to o(0.40f, 0.30f),
            o(0.62f, 0.66f) to o(0.78f, 0.60f),
            o(0.78f, 0.60f) to o(0.90f, 0.70f),
        ),
        weights = listOf(o(0.40f, 0.27f)),
    )
    ExercisePose.GENERIC -> PoseDef(
        head = o(0.5f, 0.16f), headR = 0.07f,
        bones = listOf(
            o(0.5f, 0.22f) to o(0.5f, 0.56f),
            o(0.46f, 0.24f) to o(0.36f, 0.54f),
            o(0.54f, 0.24f) to o(0.64f, 0.54f),
            o(0.5f, 0.56f) to o(0.45f, 0.92f),
            o(0.5f, 0.56f) to o(0.55f, 0.92f),
        ),
    )
}

private fun DrawScope.drawPose(def: PoseDef, color: Color, accent: Color) {
    val w = size.width
    val h = size.height
    fun p(off: Offset) = Offset(off.x * w, off.y * h)
    val boneW = h * 0.055f

    def.bars.forEach { (a, b) ->
        drawLine(accent, p(a), p(b), strokeWidth = h * 0.03f, cap = StrokeCap.Round)
    }
    def.bones.forEach { (a, b) ->
        drawLine(color, p(a), p(b), strokeWidth = boneW, cap = StrokeCap.Round)
    }
    drawCircle(color, def.headR * h, p(def.head))
    def.weights.forEach { drawCircle(accent, h * 0.05f, p(it)) }
}

@Composable
fun ExercisePoseView(
    exerciseId: String,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFB9C0DA),
    accent: Color = Color(0xFFFFB454),
) {
    val def = poseFor(exerciseId)
    Canvas(modifier) { drawPose(poseDef(def), color, accent) }
}

/**
 * Иллюстрация упражнения. Если в res/drawable есть файл `ex_<id>` (png/webp/jpg/xml),
 * показываем его. Иначе — рисуем схематичный силуэт (ExercisePoseView) как запасной вариант.
 * Это позволяет добавлять готовые картинки техники, просто кладя файлы в drawable.
 */
@Composable
fun ExerciseImage(
    exerciseId: String,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFB9C0DA),
    accent: Color = Color(0xFFFFB454),
) {
    val context = LocalContext.current
    val resId = remember(exerciseId) {
        context.resources.getIdentifier("ex_$exerciseId", "drawable", context.packageName)
    }
    if (resId != 0) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = modifier,
        )
    } else {
        ExercisePoseView(exerciseId, modifier, color, accent)
    }
}

