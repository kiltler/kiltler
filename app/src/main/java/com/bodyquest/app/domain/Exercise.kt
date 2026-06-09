package com.bodyquest.app.domain

/** Доступный инвентарь пользователя. */
enum class Equipment(val title: String) {
    KETTLEBELL("Гиря 32 кг"),
    PULLUP_DIP_BAR("Турник + брусья"),
    BANDS("Резиновые петли"),
    YOGA_MAT("Коврик для йоги"),
    SCALE("Напольные весы"),
    PUSHUP_HANDLES("Упоры для отжиманий"),
    DUMBBELLS("Гантели 8 кг ×2"),
    BODYWEIGHT("Собственный вес");
}

/** Целевые мышечные группы. */
enum class Muscle(val title: String) {
    BACK("Спина"),
    CHEST("Грудь"),
    SHOULDERS("Плечи"),
    BICEPS("Бицепс"),
    TRICEPS("Трицепс"),
    LEGS("Ноги"),
    GLUTES("Ягодицы"),
    CORE("Кор"),
    FULL_BODY("Всё тело");
}

/** Как логируется упражнение. */
enum class ExerciseType {
    BODYWEIGHT_REPS,   // повторы с весом тела (отжимания, подтягивания)
    WEIGHTED_REPS,     // вес + повторы (жим гири, сгибания)
    TIMED,             // время в секундах (планка, вис)
    MOBILITY;          // растяжка/мобилити, фиксируем по времени
}

/** Справочное упражнение (seed, хранится в коде). */
data class Exercise(
    val id: String,
    val name: String,
    val equipment: List<Equipment>,
    val muscles: List<Muscle>,
    val attribute: AttributeType,
    val type: ExerciseType,
    val instructions: String,
    val formTips: List<String>,
)

/** Запланированное упражнение в тренировочном дне. */
data class PlannedExercise(
    val exerciseId: String,
    val sets: Int,
    val targetReps: String,        // напр. "8–12" или "макс." или "40 сек"
    val restSeconds: Int,
    val note: String = "",
)

/** Тренировочный день (квест). */
data class WorkoutDay(
    val id: String,
    val title: String,
    val focus: String,
    val emphasis: List<AttributeType>,
    val warmup: List<String>,
    val exercises: List<PlannedExercise>,
    val isBoss: Boolean = false,
    val portrait: String = "",       // эмодзи-«портрет» босса
    val bossSubtitle: String = "",   // короткое описание босса
)

/** Недельная программа. */
data class Program(
    val name: String,
    val daysPerWeek: Int,
    val days: List<WorkoutDay>,
    val bosses: List<WorkoutDay>,
    val notes: List<String>,
) {
    /** Босс этой недели — ротация по номеру недели года. */
    fun bossForWeek(weekOfYear: Int): WorkoutDay =
        bosses[((weekOfYear % bosses.size) + bosses.size) % bosses.size]
}
