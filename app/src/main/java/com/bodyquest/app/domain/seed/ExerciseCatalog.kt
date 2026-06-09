package com.bodyquest.app.domain.seed

import com.bodyquest.app.domain.AttributeType
import com.bodyquest.app.domain.Equipment
import com.bodyquest.app.domain.Exercise
import com.bodyquest.app.domain.ExerciseType
import com.bodyquest.app.domain.Muscle

/**
 * Справочник упражнений. Построен ТОЛЬКО на доступном инвентаре:
 * гиря 32 кг, турник+брусья, резинки, коврик, упоры для отжиманий, гантели 8 кг.
 */
object ExerciseCatalog {

    val all: List<Exercise> = listOf(
        // ─── Спина / тяги (приоритет для V-силуэта) ─────────────────────
        Exercise(
            id = "pullup",
            name = "Подтягивания",
            equipment = listOf(Equipment.PULLUP_DIP_BAR, Equipment.BANDS),
            muscles = listOf(Muscle.BACK, Muscle.BICEPS),
            attribute = AttributeType.STRENGTH,
            type = ExerciseType.BODYWEIGHT_REPS,
            instructions = "Вис на турнике прямым хватом чуть шире плеч. Сводя лопатки, " +
                "подтянитесь грудью к перекладине, затем подконтрольно опуститесь. " +
                "Для облегчения накиньте резиновую петлю под стопу или колено.",
            formTips = listOf(
                "Тяните локти вниз к рёбрам, а не плечи к ушам.",
                "Не раскачивайтесь — корпус напряжён, без рывков."
            ),
        ),
        Exercise(
            id = "band_row",
            name = "Тяга гири в наклоне",
            equipment = listOf(Equipment.KETTLEBELL),
            muscles = listOf(Muscle.BACK, Muscle.BICEPS),
            attribute = AttributeType.STRENGTH,
            type = ExerciseType.WEIGHTED_REPS,
            instructions = "Наклонитесь с прямой спиной, гиря в одной руке. " +
                "Тяните гирю к поясу, сводя лопатку, опускайте подконтрольно.",
            formTips = listOf(
                "Спина ровная, взгляд в пол на метр впереди.",
                "Тянет спина, а не бицепс — ведите локоть назад."
            ),
        ),
        Exercise(
            id = "face_pull",
            name = "Face pull (резинка)",
            equipment = listOf(Equipment.BANDS),
            muscles = listOf(Muscle.SHOULDERS, Muscle.BACK),
            attribute = AttributeType.STRENGTH,
            type = ExerciseType.BODYWEIGHT_REPS,
            instructions = "Закрепите резинку на уровне лица. Тяните к лицу, разводя кисти " +
                "в стороны и сводя лопатки. Задержка в пике 1 сек.",
            formTips = listOf(
                "Локти держите высоко, на уровне плеч.",
                "Движение медленное — это здоровье плеч и осанка."
            ),
        ),
        Exercise(
            id = "rear_delt_band",
            name = "Разведение резинки (задние дельты)",
            equipment = listOf(Equipment.BANDS),
            muscles = listOf(Muscle.SHOULDERS, Muscle.BACK),
            attribute = AttributeType.STRENGTH,
            type = ExerciseType.BODYWEIGHT_REPS,
            instructions = "Держите резинку перед собой на прямых руках и разводите в стороны, " +
                "сводя лопатки. Возврат подконтрольно.",
            formTips = listOf("Не сгибайте локти.", "Плечи опущены, шея расслаблена."),
        ),

        // ─── Грудь / трицепс / плечи ────────────────────────────────────
        Exercise(
            id = "dips",
            name = "Отжимания на брусьях",
            equipment = listOf(Equipment.PULLUP_DIP_BAR, Equipment.BANDS),
            muscles = listOf(Muscle.CHEST, Muscle.TRICEPS),
            attribute = AttributeType.STRENGTH,
            type = ExerciseType.BODYWEIGHT_REPS,
            instructions = "Упор на брусьях, опуститесь до угла ~90° в локтях, затем выжмите вверх. " +
                "Лёгкий наклон корпуса вперёд включает грудь.",
            formTips = listOf(
                "Не проваливайте плечи слишком низко — берегите суставы.",
                "Резинка под колени облегчит движение на старте."
            ),
        ),
        Exercise(
            id = "pushup",
            name = "Отжимания с упоров",
            equipment = listOf(Equipment.PUSHUP_HANDLES, Equipment.BODYWEIGHT),
            muscles = listOf(Muscle.CHEST, Muscle.SHOULDERS, Muscle.TRICEPS),
            attribute = AttributeType.STRENGTH,
            type = ExerciseType.BODYWEIGHT_REPS,
            instructions = "Кисти на упорах, тело в прямую линию. Опускайтесь до растяжения груди, " +
                "выжимайте вверх. Упоры дают большую амплитуду.",
            formTips = listOf(
                "Таз не провисает и не задирается — корпус напряжён.",
                "Локти под ~45° к корпусу, не в стороны."
            ),
        ),
        Exercise(
            id = "kb_press",
            name = "Жим гири стоя",
            equipment = listOf(Equipment.KETTLEBELL),
            muscles = listOf(Muscle.SHOULDERS, Muscle.TRICEPS),
            attribute = AttributeType.STRENGTH,
            type = ExerciseType.WEIGHTED_REPS,
            instructions = "Гиря в стойке у плеча. Выжмите вверх до полного выпрямления руки, " +
                "опустите подконтрольно. Кор напряжён.",
            formTips = listOf("Не прогибайте поясницу.", "Запястье прямое, гиря лежит на предплечье."),
        ),

        // ─── Плечи (приоритет!) гантели 8 кг ────────────────────────────
        Exercise(
            id = "db_lateral",
            name = "Махи гантелями в стороны",
            equipment = listOf(Equipment.DUMBBELLS),
            muscles = listOf(Muscle.SHOULDERS),
            attribute = AttributeType.STRENGTH,
            type = ExerciseType.WEIGHTED_REPS,
            instructions = "Гантели по бокам, лёгкий наклон корпуса. Поднимайте руки в стороны " +
                "до уровня плеч, мизинцы чуть выше. Медленно опускайте.",
            formTips = listOf(
                "Ведёте локтями, а не кистями.",
                "Без раскачки — приоритет на средние дельты для ширины плеч."
            ),
        ),
        Exercise(
            id = "db_curl",
            name = "Сгибания на бицепс (гантели)",
            equipment = listOf(Equipment.DUMBBELLS),
            muscles = listOf(Muscle.BICEPS),
            attribute = AttributeType.STRENGTH,
            type = ExerciseType.WEIGHTED_REPS,
            instructions = "Сгибайте руки, поднимая гантели к плечам, локти прижаты к корпусу. " +
                "Опускайте подконтрольно.",
            formTips = listOf("Не раскачивайте корпус.", "В нижней точке полностью выпрямляйте руки."),
        ),
        Exercise(
            id = "db_rear_fly",
            name = "Разведение гантелей в наклоне",
            equipment = listOf(Equipment.DUMBBELLS),
            muscles = listOf(Muscle.SHOULDERS, Muscle.BACK),
            attribute = AttributeType.STRENGTH,
            type = ExerciseType.WEIGHTED_REPS,
            instructions = "Наклон корпуса вперёд с прямой спиной. Разводите гантели в стороны, " +
                "сводя лопатки. Задние дельты — ключ к объёмным плечам сзади.",
            formTips = listOf("Спина прямая.", "Лёгкий вес, чистая техника."),
        ),

        // ─── Гиря: сила/выносливость, всё тело ──────────────────────────
        Exercise(
            id = "kb_swing",
            name = "Махи гирей",
            equipment = listOf(Equipment.KETTLEBELL),
            muscles = listOf(Muscle.GLUTES, Muscle.BACK, Muscle.LEGS),
            attribute = AttributeType.ENDURANCE,
            type = ExerciseType.WEIGHTED_REPS,
            instructions = "Гиря между ног, спина прямая. Резким движением таза вперёд " +
                "выбрасывайте гирю до уровня груди. Сила из бёдер, не из рук.",
            formTips = listOf(
                "Это шарнир в тазобедренном суставе, а не присед.",
                "В верхней точке ягодицы сжаты, корпус прямой."
            ),
        ),
        Exercise(
            id = "kb_goblet_squat",
            name = "Гоблет-приседания с гирей",
            equipment = listOf(Equipment.KETTLEBELL),
            muscles = listOf(Muscle.LEGS, Muscle.GLUTES, Muscle.CORE),
            attribute = AttributeType.STRENGTH,
            type = ExerciseType.WEIGHTED_REPS,
            instructions = "Держите гирю у груди двумя руками. Приседайте до параллели или ниже, " +
                "колени по направлению носков, спина прямая.",
            formTips = listOf("Пятки не отрывайте.", "Грудь раскрыта, взгляд вперёд."),
        ),
        Exercise(
            id = "kb_clean",
            name = "Заброс гири",
            equipment = listOf(Equipment.KETTLEBELL),
            muscles = listOf(Muscle.FULL_BODY),
            attribute = AttributeType.ENDURANCE,
            type = ExerciseType.WEIGHTED_REPS,
            instructions = "С маха забросьте гирю в стойку у плеча мягко, без удара по предплечью. " +
                "Опустите обратно в мах.",
            formTips = listOf("Прижмите локоть к корпусу в стойке.", "Кисть «обнимает» гирю, не бьёт."),
        ),
        Exercise(
            id = "kb_snatch",
            name = "Рывок гири",
            equipment = listOf(Equipment.KETTLEBELL),
            muscles = listOf(Muscle.FULL_BODY, Muscle.SHOULDERS),
            attribute = AttributeType.ENDURANCE,
            type = ExerciseType.WEIGHTED_REPS,
            instructions = "Мощным махом отправьте гирю вверх в одно движение до выпрямленной руки " +
                "над головой. Опустите подконтрольно в мах.",
            formTips = listOf("Мощность из таза.", "Фиксация вверху — рука прямая, гиря над плечом."),
        ),
        Exercise(
            id = "kb_tgu",
            name = "Турецкий подъём",
            equipment = listOf(Equipment.KETTLEBELL, Equipment.YOGA_MAT),
            muscles = listOf(Muscle.FULL_BODY, Muscle.CORE, Muscle.SHOULDERS),
            attribute = AttributeType.MOBILITY,
            type = ExerciseType.WEIGHTED_REPS,
            instructions = "Из положения лёжа поднимитесь в стойку, удерживая гирю на прямой руке " +
                "над собой, проходя все промежуточные позиции. Затем обратно.",
            formTips = listOf(
                "Глаза на гирю всю первую половину.",
                "Медленно и контролируемо — это про стабильность плеча."
            ),
        ),

        // ─── Кор ────────────────────────────────────────────────────────
        Exercise(
            id = "hanging_leg_raise",
            name = "Подъёмы ног в висе",
            equipment = listOf(Equipment.PULLUP_DIP_BAR),
            muscles = listOf(Muscle.CORE),
            attribute = AttributeType.STRENGTH,
            type = ExerciseType.BODYWEIGHT_REPS,
            instructions = "Вис на турнике. Поднимайте прямые или согнутые ноги к груди, " +
                "опускайте без раскачки.",
            formTips = listOf("Не раскачивайтесь.", "Скручивайте таз, а не просто поднимайте бёдра."),
        ),
        Exercise(
            id = "plank",
            name = "Планка",
            equipment = listOf(Equipment.YOGA_MAT, Equipment.BODYWEIGHT),
            muscles = listOf(Muscle.CORE),
            attribute = AttributeType.ENDURANCE,
            type = ExerciseType.TIMED,
            instructions = "Упор на предплечьях, тело в прямую линию от пяток до головы. " +
                "Держите заданное время.",
            formTips = listOf("Таз не провисает.", "Живот втянут, ягодицы напряжены."),
        ),

        // ─── Мобильность / растяжка на коврике ──────────────────────────
        Exercise(
            id = "mobility_flow",
            name = "Мобилити-комплекс на коврике",
            equipment = listOf(Equipment.YOGA_MAT),
            muscles = listOf(Muscle.FULL_BODY),
            attribute = AttributeType.MOBILITY,
            type = ExerciseType.MOBILITY,
            instructions = "Кошка-корова, выпады с разворотом, раскрытие грудного отдела, " +
                "вращения тазом и плечами. Дышите ровно, тянитесь без боли.",
            formTips = listOf("Двигайтесь плавно.", "Не тянитесь через боль — только до лёгкого натяжения."),
        ),
        Exercise(
            id = "band_shoulder_dislocate",
            name = "Выкруты с резинкой (плечи)",
            equipment = listOf(Equipment.BANDS),
            muscles = listOf(Muscle.SHOULDERS),
            attribute = AttributeType.MOBILITY,
            type = ExerciseType.MOBILITY,
            instructions = "Держите резинку широким хватом и проводите прямые руки над головой " +
                "назад и обратно. Раскрытие грудного и плечевого пояса.",
            formTips = listOf("Руки прямые.", "Хват тем шире, чем туже ощущения."),
        ),
        Exercise(
            id = "hip_stretch",
            name = "Растяжка бёдер и сгибателей",
            equipment = listOf(Equipment.YOGA_MAT),
            muscles = listOf(Muscle.LEGS, Muscle.GLUTES),
            attribute = AttributeType.MOBILITY,
            type = ExerciseType.MOBILITY,
            instructions = "Выпад с опущенным задним коленом для сгибателей бедра, поза голубя " +
                "для ягодиц. Удерживайте растяжение, дышите.",
            formTips = listOf("Без рывков.", "Держите позицию 30–45 секунд на сторону."),
        ),
    )

    val byId: Map<String, Exercise> = all.associateBy { it.id }

    fun get(id: String): Exercise? = byId[id]

    fun forEquipment(equipment: Equipment): List<Exercise> =
        all.filter { equipment in it.equipment }
}
