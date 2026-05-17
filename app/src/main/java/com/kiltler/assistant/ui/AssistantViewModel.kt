package com.kiltler.assistant.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kiltler.assistant.backup.BackupData
import com.kiltler.assistant.backup.BackupManager
import com.kiltler.assistant.data.AppDatabase
import com.kiltler.assistant.data.Material
import com.kiltler.assistant.data.Order
import com.kiltler.assistant.data.OrderStatus
import com.kiltler.assistant.data.Reminder
import com.kiltler.assistant.data.Repository
import com.kiltler.assistant.data.WorkPlace
import com.kiltler.assistant.notifications.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AssistantViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = Repository(AppDatabase.get(app))
    private val ctx get() = getApplication<Application>()

    val reminders: StateFlow<List<Reminder>> =
        repo.reminders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val orders: StateFlow<List<Order>> =
        repo.orders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val materials: StateFlow<List<Material>> =
        repo.materials.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val workPlaces: StateFlow<List<WorkPlace>> =
        repo.workPlaces.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Напоминания ---
    fun saveReminder(reminder: Reminder) = viewModelScope.launch {
        val id = repo.upsertReminder(reminder)
        val saved = if (reminder.id == 0L) reminder.copy(id = id) else reminder
        val code = ReminderScheduler.reminderRequestCode(saved.id)
        if (saved.isDone) {
            ReminderScheduler.cancel(ctx, code)
        } else {
            ReminderScheduler.schedule(ctx, code, saved.timeMillis, saved.title, saved.description)
        }
    }

    fun toggleReminderDone(reminder: Reminder) =
        saveReminder(reminder.copy(isDone = !reminder.isDone))

    fun deleteReminder(reminder: Reminder) = viewModelScope.launch {
        repo.deleteReminder(reminder)
        ReminderScheduler.cancel(ctx, ReminderScheduler.reminderRequestCode(reminder.id))
    }

    // --- Заказы ---
    fun saveOrder(order: Order) = viewModelScope.launch {
        val id = repo.upsertOrder(order)
        val saved = if (order.id == 0L) order.copy(id = id) else order
        syncOrderReminder(saved)
    }

    fun deleteOrder(order: Order) = viewModelScope.launch {
        repo.deleteOrder(order)
        ReminderScheduler.cancel(ctx, ReminderScheduler.orderRequestCode(order.id))
    }

    private fun syncOrderReminder(order: Order) {
        val code = ReminderScheduler.orderRequestCode(order.id)
        val status = OrderStatus.from(order.status)
        val active = status != OrderStatus.DONE && status != OrderStatus.CANCELLED
        val time = order.scheduledMillis
        if (order.reminderEnabled && active && time != null) {
            val text = listOfNotNull(
                order.address.ifBlank { null },
                order.description.ifBlank { null }
            ).joinToString(" — ").ifBlank { "Запланированный заказ" }
            ReminderScheduler.schedule(ctx, code, time, "Заказ: ${order.clientName}", text)
        } else {
            ReminderScheduler.cancel(ctx, code)
        }
    }

    // --- Материалы ---
    fun saveMaterial(material: Material) = viewModelScope.launch {
        repo.upsertMaterial(material)
    }

    fun deleteMaterial(material: Material) = viewModelScope.launch {
        repo.deleteMaterial(material)
    }

    fun adjustMaterial(material: Material, delta: Double) = viewModelScope.launch {
        val updated = material.copy(quantity = (material.quantity + delta).coerceAtLeast(0.0))
        repo.upsertMaterial(updated)
    }

    // --- Места работы ---
    fun saveWorkPlace(place: WorkPlace) = viewModelScope.launch {
        repo.upsertWorkPlace(place)
    }

    fun deleteWorkPlace(place: WorkPlace) = viewModelScope.launch {
        repo.deleteWorkPlace(place)
    }

    // --- Бэкап ---
    suspend fun buildBackup(): BackupData = BackupData(
        reminders = repo.allReminders(),
        orders = repo.allOrders(),
        materials = repo.allMaterials(),
        workPlaces = repo.allWorkPlaces()
    )

    fun importBackup(json: String, onResult: (Boolean, String) -> Unit) = viewModelScope.launch {
        try {
            val data = BackupManager.fromJson(json)
            repo.replaceAll(data.reminders, data.orders, data.materials, data.workPlaces)
            data.reminders.filter { !it.isDone }.forEach {
                ReminderScheduler.schedule(
                    ctx, ReminderScheduler.reminderRequestCode(it.id),
                    it.timeMillis, it.title, it.description
                )
            }
            data.orders.forEach { syncOrderReminder(it) }
            onResult(true, "Данные импортированы")
        } catch (e: Exception) {
            onResult(false, "Ошибка импорта: ${e.message}")
        }
    }
}
