package com.kiltler.assistant.data

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Напоминание из графика. */
@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val timeMillis: Long,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/** Заказ клиента — ручная мини-CRM. */
@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientName: String,
    val phone: String = "",
    val address: String = "",
    val apartment: String = "",
    val entrance: String = "",
    val description: String = "",
    val price: Double = 0.0,
    /** Код модели кондиционера при продаже (MDV7/MDV9/MDV12/MDV24/MULTI или ""). */
    val acModel: String = "",
    /** Наценка на одну единицу кондиционера, ₽. */
    val acMargin: Double = 0.0,
    val status: String = OrderStatus.NEW.name,
    val scheduledMillis: Long? = null,
    val reminderEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

/** Позиция склада — остатки материалов. */
@Entity(tableName = "materials")
data class Material(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val unit: String = "шт",
    val quantity: Double = 0.0,
    val minQuantity: Double = 0.0,
    val note: String = ""
) {
    val isLow: Boolean get() = quantity <= minQuantity
}

/** Точка на карте — место работы. */
@Entity(tableName = "workplaces")
data class WorkPlace(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val latitude: Double,
    val longitude: Double,
    val createdAt: Long = System.currentTimeMillis()
)

/** Стадии заказа в CRM. */
enum class OrderStatus(val label: String, val color: Color) {
    NEW("Новый", Color(0xFF1F82E0)),
    IN_PROGRESS("В работе", Color(0xFFE08A1F)),
    DONE("Выполнен", Color(0xFF2E9E5B)),
    CANCELLED("Отменён", Color(0xFF9AA0A6));

    companion object {
        fun from(raw: String): OrderStatus =
            entries.firstOrNull { it.name == raw } ?: NEW
    }
}
