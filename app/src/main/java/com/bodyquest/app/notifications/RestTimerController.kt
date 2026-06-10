package com.bodyquest.app.notifications

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Процессно-глобальное состояние таймера отдыха (источник истины — сервис). */
object RestTimerController {
    private val _remaining = MutableStateFlow(0)
    val remaining: StateFlow<Int> = _remaining
    fun set(value: Int) { _remaining.value = value }
}
