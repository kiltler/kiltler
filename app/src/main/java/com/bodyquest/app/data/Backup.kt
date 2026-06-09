package com.bodyquest.app.data

import org.json.JSONArray
import org.json.JSONObject

/** Срез всех данных для бэкапа/восстановления. */
data class BackupData(
    val profile: UserProfileEntity?,
    val measurements: List<MeasurementEntity>,
    val sessions: List<WorkoutSessionEntity>,
    val sets: List<SetEntity>,
    val prs: List<ExercisePrEntity>,
    val achievements: List<AchievementEntity>,
    val streak: StreakEntity?,
    val settings: SettingsEntity?,
    val water: List<WaterEntity>,
)

/**
 * Сериализация прогресса в JSON и обратно. Локальный файл-бэкап, который можно
 * положить в любое облако (Google Drive и т.п.). Без сторонних библиотек — org.json.
 */
object BackupSerializer {

    const val FORMAT_VERSION = 1

    fun export(data: BackupData): String {
        val root = JSONObject()
        root.put("formatVersion", FORMAT_VERSION)
        root.put("exportedAt", System.currentTimeMillis())

        data.profile?.let { p ->
            root.put("profile", JSONObject().apply {
                put("id", p.id); put("name", p.name); put("heightCm", p.heightCm)
                put("age", p.age); put("sexMale", p.sexMale); put("daysPerWeek", p.daysPerWeek)
                put("activity", p.activity); put("onboarded", p.onboarded); put("createdAtMillis", p.createdAtMillis)
            })
        }

        root.put("measurements", JSONArray().apply {
            data.measurements.forEach { m ->
                put(JSONObject().apply {
                    put("id", m.id); put("dateEpochDay", m.dateEpochDay); put("dateMillis", m.dateMillis)
                    put("weightKg", m.weightKg); put("chest", m.chest); put("shoulders", m.shoulders)
                    put("belly", m.belly); put("waist", m.waist); put("thigh", m.thigh)
                    put("hips", m.hips); put("inseam", m.inseam); put("foot", m.foot)
                    put("xpComposition", m.xpComposition)
                })
            }
        })

        root.put("sessions", JSONArray().apply {
            data.sessions.forEach { s ->
                put(JSONObject().apply {
                    put("id", s.id); put("dayId", s.dayId); put("title", s.title)
                    put("dateMillis", s.dateMillis); put("dateEpochDay", s.dateEpochDay)
                    put("durationSeconds", s.durationSeconds); put("isBoss", s.isBoss)
                    put("xpStrength", s.xpStrength); put("xpEndurance", s.xpEndurance)
                    put("xpMobility", s.xpMobility); put("xpDiscipline", s.xpDiscipline)
                    put("totalXp", s.totalXp); put("streakMultiplierX100", s.streakMultiplierX100)
                })
            }
        })

        root.put("sets", JSONArray().apply {
            data.sets.forEach { s ->
                put(JSONObject().apply {
                    put("id", s.id); put("sessionId", s.sessionId); put("exerciseId", s.exerciseId)
                    put("exerciseName", s.exerciseName); put("dateMillis", s.dateMillis)
                    put("setIndex", s.setIndex); put("reps", s.reps); put("weightKg", s.weightKg)
                    put("timeSeconds", s.timeSeconds)
                })
            }
        })

        root.put("prs", JSONArray().apply {
            data.prs.forEach { p ->
                put(JSONObject().apply {
                    put("exerciseId", p.exerciseId); put("bestWeight", p.bestWeight); put("bestReps", p.bestReps)
                    put("bestTimeSeconds", p.bestTimeSeconds); put("lastWeight", p.lastWeight)
                    put("lastReps", p.lastReps); put("lastTimeSeconds", p.lastTimeSeconds)
                    put("updatedAtMillis", p.updatedAtMillis)
                })
            }
        })

        root.put("achievements", JSONArray().apply {
            data.achievements.forEach { a ->
                put(JSONObject().apply {
                    put("id", a.id); put("unlocked", a.unlocked); put("unlockedAtMillis", a.unlockedAtMillis)
                })
            }
        })

        data.streak?.let { st ->
            root.put("streak", JSONObject().apply {
                put("id", st.id); put("current", st.current); put("longest", st.longest)
                put("lastWorkoutEpochDay", st.lastWorkoutEpochDay)
            })
        }

        data.settings?.let { s ->
            root.put("settings", JSONObject().apply {
                put("id", s.id); put("remindersEnabled", s.remindersEnabled)
                put("reminderHour", s.reminderHour); put("reminderMinute", s.reminderMinute)
                put("waterRemindersEnabled", s.waterRemindersEnabled)
            })
        }

        root.put("water", JSONArray().apply {
            data.water.forEach { w ->
                put(JSONObject().apply { put("dateEpochDay", w.dateEpochDay); put("amountMl", w.amountMl) })
            }
        })

        return root.toString()
    }

    fun parse(json: String): BackupData {
        val root = JSONObject(json)

        val profile = root.optJSONObject("profile")?.let { p ->
            UserProfileEntity(
                id = p.optInt("id", 1), name = p.optString("name", "Герой"),
                heightCm = p.optInt("heightCm", 189), age = p.optInt("age", 30),
                sexMale = p.optBoolean("sexMale", true), daysPerWeek = p.optInt("daysPerWeek", 3),
                activity = p.optString("activity", "LIGHT"), onboarded = p.optBoolean("onboarded", true),
                createdAtMillis = p.optLong("createdAtMillis", 0L),
            )
        }

        val measurements = root.optJSONArray("measurements").mapObjects { m ->
            MeasurementEntity(
                id = m.optLong("id", 0), dateEpochDay = m.optLong("dateEpochDay", 0),
                dateMillis = m.optLong("dateMillis", 0), weightKg = m.optDouble("weightKg", 0.0),
                chest = m.optDouble("chest", 0.0), shoulders = m.optDouble("shoulders", 0.0),
                belly = m.optDouble("belly", 0.0), waist = m.optDouble("waist", 0.0),
                thigh = m.optDouble("thigh", 0.0), hips = m.optDouble("hips", 0.0),
                inseam = m.optDouble("inseam", 0.0), foot = m.optDouble("foot", 0.0),
                xpComposition = m.optInt("xpComposition", 0),
            )
        }

        val sessions = root.optJSONArray("sessions").mapObjects { s ->
            WorkoutSessionEntity(
                id = s.optLong("id", 0), dayId = s.optString("dayId", ""), title = s.optString("title", ""),
                dateMillis = s.optLong("dateMillis", 0), dateEpochDay = s.optLong("dateEpochDay", 0),
                durationSeconds = s.optInt("durationSeconds", 0), isBoss = s.optBoolean("isBoss", false),
                xpStrength = s.optInt("xpStrength", 0), xpEndurance = s.optInt("xpEndurance", 0),
                xpMobility = s.optInt("xpMobility", 0), xpDiscipline = s.optInt("xpDiscipline", 0),
                totalXp = s.optInt("totalXp", 0), streakMultiplierX100 = s.optInt("streakMultiplierX100", 100),
            )
        }

        val sets = root.optJSONArray("sets").mapObjects { s ->
            SetEntity(
                id = s.optLong("id", 0), sessionId = s.optLong("sessionId", 0),
                exerciseId = s.optString("exerciseId", ""), exerciseName = s.optString("exerciseName", ""),
                dateMillis = s.optLong("dateMillis", 0), setIndex = s.optInt("setIndex", 0),
                reps = s.optInt("reps", 0), weightKg = s.optDouble("weightKg", 0.0),
                timeSeconds = s.optInt("timeSeconds", 0),
            )
        }

        val prs = root.optJSONArray("prs").mapObjects { p ->
            ExercisePrEntity(
                exerciseId = p.optString("exerciseId", ""), bestWeight = p.optDouble("bestWeight", 0.0),
                bestReps = p.optInt("bestReps", 0), bestTimeSeconds = p.optInt("bestTimeSeconds", 0),
                lastWeight = p.optDouble("lastWeight", 0.0), lastReps = p.optInt("lastReps", 0),
                lastTimeSeconds = p.optInt("lastTimeSeconds", 0), updatedAtMillis = p.optLong("updatedAtMillis", 0),
            )
        }

        val achievements = root.optJSONArray("achievements").mapObjects { a ->
            AchievementEntity(
                id = a.optString("id", ""), unlocked = a.optBoolean("unlocked", false),
                unlockedAtMillis = a.optLong("unlockedAtMillis", 0),
            )
        }.filter { it.id.isNotEmpty() }

        val streak = root.optJSONObject("streak")?.let { st ->
            StreakEntity(
                id = st.optInt("id", 1), current = st.optInt("current", 0),
                longest = st.optInt("longest", 0), lastWorkoutEpochDay = st.optLong("lastWorkoutEpochDay", -1),
            )
        }

        val settings = root.optJSONObject("settings")?.let { s ->
            SettingsEntity(
                id = s.optInt("id", 1), remindersEnabled = s.optBoolean("remindersEnabled", false),
                reminderHour = s.optInt("reminderHour", 18), reminderMinute = s.optInt("reminderMinute", 30),
                waterRemindersEnabled = s.optBoolean("waterRemindersEnabled", false),
            )
        }

        val water = root.optJSONArray("water").mapObjects { w ->
            WaterEntity(dateEpochDay = w.optLong("dateEpochDay", 0), amountMl = w.optInt("amountMl", 0))
        }

        return BackupData(profile, measurements, sessions, sets, prs, achievements, streak, settings, water)
    }

    private inline fun <T> JSONArray?.mapObjects(transform: (JSONObject) -> T): List<T> {
        if (this == null) return emptyList()
        val out = ArrayList<T>(length())
        for (i in 0 until length()) {
            optJSONObject(i)?.let { out.add(transform(it)) }
        }
        return out
    }
}
