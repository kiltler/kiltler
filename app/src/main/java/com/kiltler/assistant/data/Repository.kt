package com.kiltler.assistant.data

import kotlinx.coroutines.flow.Flow

/** Единая точка доступа к данным поверх Room. */
class Repository(db: AppDatabase) {

    private val reminderDao = db.reminderDao()
    private val orderDao = db.orderDao()
    private val materialDao = db.materialDao()
    private val workPlaceDao = db.workPlaceDao()
    private val expenseDao = db.expenseDao()

    val reminders: Flow<List<Reminder>> = reminderDao.observeAll()
    val orders: Flow<List<Order>> = orderDao.observeAll()
    val materials: Flow<List<Material>> = materialDao.observeAll()
    val workPlaces: Flow<List<WorkPlace>> = workPlaceDao.observeAll()
    val expenses: Flow<List<Expense>> = expenseDao.observeAll()

    // --- Напоминания ---
    suspend fun upsertReminder(r: Reminder): Long = reminderDao.upsert(r)
    suspend fun deleteReminder(r: Reminder) = reminderDao.delete(r)
    suspend fun allReminders(): List<Reminder> = reminderDao.getAll()

    // --- Заказы ---
    suspend fun upsertOrder(o: Order): Long = orderDao.upsert(o)
    suspend fun deleteOrder(o: Order) = orderDao.delete(o)
    suspend fun allOrders(): List<Order> = orderDao.getAll()

    // --- Материалы ---
    suspend fun upsertMaterial(m: Material): Long = materialDao.upsert(m)
    suspend fun deleteMaterial(m: Material) = materialDao.delete(m)
    suspend fun allMaterials(): List<Material> = materialDao.getAll()

    // --- Места работы ---
    suspend fun upsertWorkPlace(p: WorkPlace): Long = workPlaceDao.upsert(p)
    suspend fun deleteWorkPlace(p: WorkPlace) = workPlaceDao.delete(p)
    suspend fun allWorkPlaces(): List<WorkPlace> = workPlaceDao.getAll()

    // --- Расходы ---
    suspend fun upsertExpense(e: Expense): Long = expenseDao.upsert(e)
    suspend fun deleteExpense(e: Expense) = expenseDao.delete(e)
    suspend fun allExpenses(): List<Expense> = expenseDao.getAll()

    /** Полная замена данных при импорте бэкапа. */
    suspend fun replaceAll(
        reminders: List<Reminder>,
        orders: List<Order>,
        materials: List<Material>,
        workPlaces: List<WorkPlace>,
        expenses: List<Expense>
    ) {
        reminderDao.clear(); orderDao.clear(); materialDao.clear()
        workPlaceDao.clear(); expenseDao.clear()
        reminderDao.insertAll(reminders)
        orderDao.insertAll(orders)
        materialDao.insertAll(materials)
        workPlaceDao.insertAll(workPlaces)
        expenseDao.insertAll(expenses)
    }
}
