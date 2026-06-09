package com.bodyquest.app.domain.seed

import com.bodyquest.app.domain.AttributeType
import com.bodyquest.app.domain.PlannedExercise
import com.bodyquest.app.domain.Program
import com.bodyquest.app.domain.WorkoutDay

/**
 * Программы под инвентарь пользователя. Акцент на плечи и спину для V-силуэта,
 * узкая талия за счёт кора и рекомпозиции.
 */
object ProgramSeed {

    private val standardWarmup = listOf(
        "5 мин лёгкое кардио / суставная разминка",
        "Вращения плечами и тазом — по 10 в каждую сторону",
        "Выкруты с резинкой — 15 повторов",
        "2 разминочных подхода первого упражнения с малым весом",
    )

    // ─────────────────── 3 дня в неделю ───────────────────
    private val day3Pull = WorkoutDay(
        id = "d3_pull",
        title = "День 1 — Спина и плечи (V-тяга)",
        focus = "Ширина спины, задние дельты, бицепс",
        emphasis = listOf(AttributeType.STRENGTH),
        warmup = standardWarmup,
        exercises = listOf(
            PlannedExercise("pullup", 4, "макс. (резинка по силам)", 120, "Главное движение для ширины спины"),
            PlannedExercise("band_row", 3, "10–12 на руку", 90),
            PlannedExercise("db_rear_fly", 3, "12–15", 60, "Задние дельты — объём плеч"),
            PlannedExercise("face_pull", 3, "15–20", 60, "Здоровье плеч и осанка"),
            PlannedExercise("db_curl", 3, "10–12", 60),
            PlannedExercise("hanging_leg_raise", 3, "10–15", 60, "Кор — узкая талия"),
        ),
    )

    private val day3Push = WorkoutDay(
        id = "d3_push",
        title = "День 2 — Грудь, плечи, трицепс",
        focus = "Жимовая сила, средние дельты",
        emphasis = listOf(AttributeType.STRENGTH),
        warmup = standardWarmup,
        exercises = listOf(
            PlannedExercise("dips", 4, "8–12 (резинка по силам)", 120),
            PlannedExercise("pushup", 3, "12–20", 90),
            PlannedExercise("kb_press", 3, "6–8 на руку", 90, "Жимовая сила плеч"),
            PlannedExercise("db_lateral", 4, "12–15", 45, "ПРИОРИТЕТ: ширина плеч"),
            PlannedExercise("plank", 3, "40–60 сек", 45),
        ),
    )

    private val day3Legs = WorkoutDay(
        id = "d3_legs",
        title = "День 3 — Ноги и кондиция (гиря)",
        focus = "Ноги, ягодицы, выносливость, жиросжигание",
        emphasis = listOf(AttributeType.ENDURANCE, AttributeType.STRENGTH),
        warmup = standardWarmup,
        exercises = listOf(
            PlannedExercise("kb_goblet_squat", 4, "10–12", 90),
            PlannedExercise("kb_swing", 5, "15–20", 75, "Жиросжигание + задняя цепь"),
            PlannedExercise("kb_clean", 3, "8 на руку", 75),
            PlannedExercise("mobility_flow", 1, "8–10 мин", 0, "Заминка-мобилити"),
        ),
    )

    // ─────────────────── 4 дня в неделю ───────────────────
    private val day4Pull = day3Pull.copy(id = "d4_pull", title = "День 1 — Спина (ширина)")
    private val day4Push = WorkoutDay(
        id = "d4_push",
        title = "День 2 — Грудь и трицепс",
        focus = "Жимовая сила",
        emphasis = listOf(AttributeType.STRENGTH),
        warmup = standardWarmup,
        exercises = listOf(
            PlannedExercise("dips", 4, "8–12", 120),
            PlannedExercise("pushup", 4, "12–20", 75),
            PlannedExercise("kb_press", 3, "6–8 на руку", 90),
            PlannedExercise("plank", 3, "45–60 сек", 45),
        ),
    )
    private val day4Legs = day3Legs.copy(id = "d4_legs", title = "День 3 — Ноги и кондиция")
    private val day4Shoulders = WorkoutDay(
        id = "d4_shoulders",
        title = "День 4 — Плечи и руки (V-акцент)",
        focus = "Дельты со всех сторон, руки, кор",
        emphasis = listOf(AttributeType.STRENGTH),
        warmup = standardWarmup,
        exercises = listOf(
            PlannedExercise("db_lateral", 5, "12–15", 45, "ПРИОРИТЕТ: ширина плеч"),
            PlannedExercise("kb_press", 3, "6–8 на руку", 90),
            PlannedExercise("db_rear_fly", 4, "12–15", 45),
            PlannedExercise("face_pull", 3, "15–20", 45),
            PlannedExercise("db_curl", 3, "10–12", 60),
            PlannedExercise("hanging_leg_raise", 4, "10–15", 60),
        ),
    )

    /** Лёгкий мобилити-квест на день отдыха. */
    val mobilityDay = WorkoutDay(
        id = "mobility_only",
        title = "Лёгкая мобильность",
        focus = "Восстановление и подвижность суставов",
        emphasis = listOf(AttributeType.MOBILITY),
        warmup = listOf("Спокойное глубокое дыхание 1–2 минуты"),
        exercises = listOf(
            PlannedExercise("mobility_flow", 1, "8–10 мин", 0),
            PlannedExercise("band_shoulder_dislocate", 2, "15", 30),
            PlannedExercise("hip_stretch", 2, "по 40 сек/сторона", 30),
        ),
    )

    private val bossForge = WorkoutDay(
        id = "boss_forge",
        title = "БОСС — «Кузница V»",
        focus = "Круговой челлендж на всё тело. 5 кругов, отдых только между кругами.",
        emphasis = listOf(AttributeType.ENDURANCE, AttributeType.STRENGTH, AttributeType.DISCIPLINE),
        warmup = standardWarmup,
        isBoss = true,
        portrait = "🗿",
        bossSubtitle = "Каменный страж V-силуэта",
        exercises = listOf(
            PlannedExercise("kb_swing", 5, "20", 0, "Круг 1/5 → 5/5"),
            PlannedExercise("pullup", 5, "макс.", 0),
            PlannedExercise("dips", 5, "макс.", 0),
            PlannedExercise("kb_goblet_squat", 5, "15", 0),
            PlannedExercise("plank", 5, "60 сек", 120, "Отдых 2 мин МЕЖДУ кругами"),
        ),
    )

    private val bossApe = WorkoutDay(
        id = "boss_ape",
        title = "БОСС — «Турникмен»",
        focus = "Тяги и жимы на турнике. 6 кругов на спину, грудь и руки.",
        emphasis = listOf(AttributeType.STRENGTH, AttributeType.DISCIPLINE),
        warmup = standardWarmup,
        isBoss = true,
        portrait = "🦍",
        bossSubtitle = "Царь турника",
        exercises = listOf(
            PlannedExercise("pullup", 6, "макс.", 0, "Круг 1/6 → 6/6"),
            PlannedExercise("dips", 6, "макс.", 0),
            PlannedExercise("hanging_leg_raise", 6, "12", 0),
            PlannedExercise("face_pull", 6, "20", 90, "Отдых 90 сек между кругами"),
        ),
    )

    private val bossStorm = WorkoutDay(
        id = "boss_storm",
        title = "БОСС — «Гиревой шторм»",
        focus = "10 минут непрерывной работы с гирей. На время, без остановок.",
        emphasis = listOf(AttributeType.ENDURANCE, AttributeType.STRENGTH),
        warmup = standardWarmup,
        isBoss = true,
        portrait = "🌪️",
        bossSubtitle = "Буря из 32 кг",
        exercises = listOf(
            PlannedExercise("kb_snatch", 5, "10 на руку", 0, "Меняй руки без отдыха"),
            PlannedExercise("kb_clean", 5, "10 на руку", 0),
            PlannedExercise("kb_swing", 5, "25", 60, "Отдых 60 сек между блоками"),
        ),
    )

    private val bossInferno = WorkoutDay(
        id = "boss_inferno",
        title = "БОСС — «Адское пламя»",
        focus = "Жиросжигающий ад: 7 кругов отжиманий, приседов и планки.",
        emphasis = listOf(AttributeType.ENDURANCE, AttributeType.DISCIPLINE),
        warmup = standardWarmup,
        isBoss = true,
        portrait = "🔥",
        bossSubtitle = "Пожиратель жира",
        exercises = listOf(
            PlannedExercise("pushup", 7, "макс.", 0, "Круг 1/7 → 7/7"),
            PlannedExercise("kb_goblet_squat", 7, "15", 0),
            PlannedExercise("kb_swing", 7, "20", 0),
            PlannedExercise("plank", 7, "45 сек", 75),
        ),
    )

    private val allBosses = listOf(bossForge, bossApe, bossStorm, bossInferno)

    private val commonNotes = listOf(
        "Прогрессия: как только выполняете верх диапазона повторов во всех подходах — добавьте вес/повтор или усложните вариант.",
        "Дни отдыха обязательны: минимум 1 день между тяжёлыми тренировками.",
        "Каждые 5–6 недель — неделя разгрузки (deload): −40% объёма, веса полегче.",
        "Босс недели — 1 раз в неделю в свежий день, не подряд с тяжёлой тренировкой.",
        "Тренируйтесь без боли. V-силуэт строится на плечах + спине и узкой талии.",
    )

    fun programFor(daysPerWeek: Int): Program = when (daysPerWeek) {
        4 -> Program(
            name = "V-Forge 4× (рекомпозиция)",
            daysPerWeek = 4,
            days = listOf(day4Pull, day4Push, day4Legs, day4Shoulders),
            bosses = allBosses,
            notes = commonNotes,
        )
        else -> Program(
            name = "V-Forge 3× (рекомпозиция)",
            daysPerWeek = 3,
            days = listOf(day3Pull, day3Push, day3Legs),
            bosses = allBosses,
            notes = commonNotes,
        )
    }
}
