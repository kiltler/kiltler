package com.kiltler.assistant.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY timeMillis ASC")
    fun observeAll(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders")
    suspend fun getAll(): List<Reminder>

    @Upsert
    suspend fun upsert(reminder: Reminder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reminders: List<Reminder>)

    @Delete
    suspend fun delete(reminder: Reminder)

    @Query("DELETE FROM reminders")
    suspend fun clear()
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Order>>

    @Query("SELECT * FROM orders")
    suspend fun getAll(): List<Order>

    @Upsert
    suspend fun upsert(order: Order): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(orders: List<Order>)

    @Delete
    suspend fun delete(order: Order)

    @Query("DELETE FROM orders")
    suspend fun clear()
}

@Dao
interface MaterialDao {
    @Query("SELECT * FROM materials ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<Material>>

    @Query("SELECT * FROM materials")
    suspend fun getAll(): List<Material>

    @Upsert
    suspend fun upsert(material: Material): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(materials: List<Material>)

    @Delete
    suspend fun delete(material: Material)

    @Query("DELETE FROM materials")
    suspend fun clear()
}

@Dao
interface WorkPlaceDao {
    @Query("SELECT * FROM workplaces ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<WorkPlace>>

    @Query("SELECT * FROM workplaces")
    suspend fun getAll(): List<WorkPlace>

    @Upsert
    suspend fun upsert(place: WorkPlace): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(places: List<WorkPlace>)

    @Delete
    suspend fun delete(place: WorkPlace)

    @Query("DELETE FROM workplaces")
    suspend fun clear()
}
