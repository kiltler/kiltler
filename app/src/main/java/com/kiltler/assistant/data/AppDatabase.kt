package com.kiltler.assistant.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Reminder::class, Order::class, Material::class, WorkPlace::class],
    version = 1,
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

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "assistant.db"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
    }
}
