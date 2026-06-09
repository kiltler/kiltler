package com.bodyquest.app

import android.app.Application
import android.content.Context
import com.bodyquest.app.data.AppDatabase
import com.bodyquest.app.data.Repository
import com.bodyquest.app.notifications.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Простой ручной DI-контейнер (без Hilt). */
class AppContainer(context: Context) {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: AppDatabase = AppDatabase.get(context)
    val repository: Repository = Repository(database)
    val reminderScheduler: ReminderScheduler = ReminderScheduler(context.applicationContext)

    init {
        appScope.launch { repository.seedIfNeeded() }
    }
}

class BodyQuestApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
