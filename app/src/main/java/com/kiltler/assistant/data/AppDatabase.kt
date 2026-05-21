package com.kiltler.assistant.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Reminder::class, Order::class, Material::class, WorkPlace::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun orderDao(): OrderDao
    abstract fun materialDao(): MaterialDao
    abstract fun workPlaceDao(): WorkPlaceDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        /** v2: добавлены поля «квартира» и «подъезд» в заказы. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE orders ADD COLUMN apartment TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE orders ADD COLUMN entrance TEXT NOT NULL DEFAULT ''")
            }
        }

        /** v3: добавлены модель кондиционера и наценка при продаже. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE orders ADD COLUMN acModel TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE orders ADD COLUMN acMargin REAL NOT NULL DEFAULT 0")
            }
        }

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "assistant.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
    }
}
