package com.kiltler.assistant.backup

import com.kiltler.assistant.data.Expense
import com.kiltler.assistant.data.Material
import com.kiltler.assistant.data.Order
import com.kiltler.assistant.data.Reminder
import com.kiltler.assistant.data.WorkPlace

/** Снимок всех данных приложения для экспорта/импорта. */
data class BackupData(
    val version: Int = 2,
    val exportedAt: Long = System.currentTimeMillis(),
    val reminders: List<Reminder> = emptyList(),
    val orders: List<Order> = emptyList(),
    val materials: List<Material> = emptyList(),
    val workPlaces: List<WorkPlace> = emptyList(),
    val expenses: List<Expense> = emptyList()
)
