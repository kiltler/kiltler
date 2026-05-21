package com.kiltler.assistant.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kiltler.assistant.backup.BackupData
import com.kiltler.assistant.backup.BackupManager
import com.kiltler.assistant.data.AppDatabase
import com.kiltler.assistant.data.Expense
import com.kiltler.assistant.data.Material
import com.kiltler.assistant.data.Order
import com.kiltler.assistant.data.OrderStatus
import com.kiltler.assistant.data.Reminder
import com.kiltler.assistant.data.Repository
import com.kiltler.assistant.data.WorkPlace
import com.kiltler.assistant.notifications.ReminderScheduler
import com.kiltler.assistant.sync.SyncManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Типовой набор расходников для монтажа сплит-системы: пара «название — единица». */
private val SPLIT_SYSTEM_KIT = listOf(
    "Медная труба" to "пог. метр",
    "Теплоизоляция" to "пог. метр",
    "Кабель" to "пог. метр",
    "Дренаж жидкий" to "пог. метр",
    "Метапол" to "пог. метр",
    "Кронштейны" to "комплект (пара)"
)

class AssistantViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = Repository(AppDatabase.get(app))
    private val ctx get() = getApplication<Application>()
    private val sync = SyncManager(app)

    private val _syncEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
    /** Сообщения для пользователя про облачную синхронизацию (например, ошибки парсинга). */
    val syncEvents: SharedFlow<String> = _syncEvents.asSharedFlow()

    init {
        if (sync.enabled) {
            sync.start(onRemote = { applyRemote(it) }, onEmpty = { pushSync() })
        }
    }

    val reminders: StateFlow<List<Reminder>> =
        repo.reminders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val orders: StateFlow<List<Order>> =
        repo.orders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val materials: StateFlow<List<Material>> =
        repo.materials.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val workPlaces: StateFlow<List<WorkPlace>> =
        repo.workPlaces.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val expenses: StateFlow<List<Expense>> =
        repo.expenses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
        pushSync()
    }

    fun toggleReminderDone(reminder: Reminder) =
        saveReminder(reminder.copy(isDone = !reminder.isDone))

    fun deleteReminder(reminder: Reminder) = viewModelScope.launch {
        repo.deleteReminder(reminder)
        ReminderScheduler.cancel(ctx, ReminderScheduler.reminderRequestCode(reminder.id))
        pushSync()
    }

    // --- Заказы ---
    fun saveOrder(order: Order) = viewModelScope.launch {
        val id = repo.upsertOrder(order)
        val saved = if (order.id == 0L) order.copy(id = id) else order
        syncOrderReminder(saved)
        pushSync()
    }

    fun deleteOrder(order: Order) = viewModelScope.launch {
        repo.deleteOrder(order)
        ReminderScheduler.cancel(ctx, ReminderScheduler.orderRequestCode(order.id))
        pushSync()
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
        pushSync()
    }

    fun deleteMaterial(material: Material) = viewModelScope.launch {
        repo.deleteMaterial(material)
        pushSync()
    }

    fun adjustMaterial(material: Material, delta: Double) = viewModelScope.launch {
        val updated = material.copy(quantity = (material.quantity + delta).coerceAtLeast(0.0))
        repo.upsertMaterial(updated)
        pushSync()
    }

    /** Добавляет расходники для сплит-системы, пропуская уже существующие по названию. */
    fun addSplitSystemMaterials() = viewModelScope.launch {
        val existing = repo.allMaterials().mapTo(HashSet()) { it.name.trim().lowercase() }
        SPLIT_SYSTEM_KIT
            .filter { it.first.lowercase() !in existing }
            .forEach { (name, unit) -> repo.upsertMaterial(Material(name = name, unit = unit)) }
        pushSync()
    }

    // --- Места работы ---
    fun saveWorkPlace(place: WorkPlace) = viewModelScope.launch {
        repo.upsertWorkPlace(place)
        pushSync()
    }

    fun deleteWorkPlace(place: WorkPlace) = viewModelScope.launch {
        repo.deleteWorkPlace(place)
        pushSync()
    }

    // --- Расходы ---
    fun saveExpense(expense: Expense) = viewModelScope.launch {
        repo.upsertExpense(expense)
        pushSync()
    }

    fun deleteExpense(expense: Expense) = viewModelScope.launch {
        repo.deleteExpense(expense)
        pushSync()
    }

    // --- Бэкап ---
    suspend fun buildBackup(): BackupData = BackupData(
        reminders = repo.allReminders(),
        orders = repo.allOrders(),
        materials = repo.allMaterials(),
        workPlaces = repo.allWorkPlaces(),
        expenses = repo.allExpenses()
    )

    fun importBackup(json: String, onResult: (Boolean, String) -> Unit) = viewModelScope.launch {
        try {
            val data = BackupManager.fromJson(json)
            repo.replaceAll(
                data.reminders, data.orders, data.materials, data.workPlaces, data.expenses
            )
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

    // --- Облачная синхронизация ---
    fun currentSyncCode(): String? = sync.code

    fun enableSync(code: String) {
        sync.setCode(code, onRemote = { applyRemote(it) }, onEmpty = { pushSync() })
    }

    fun disableSync() = sync.disable()

    /** Применяет данные, пришедшие из облака. Ошибки парсинга больше не глотаем. */
    private fun applyRemote(json: String) {
        importBackup(json) { ok, message ->
            if (!ok) _syncEvents.tryEmit("Облачные данные: $message")
        }
    }

    /** Выгружает текущий снимок данных в облако, если синхронизация включена. */
    private fun pushSync() {
        if (!sync.enabled) return
        viewModelScope.launch {
            sync.push(BackupManager.toJson(buildBackup()))
        }
    }
}
