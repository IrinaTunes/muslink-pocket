package ru.muslink.pocket.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class Prefs(context: Context) {

    private val sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    private val _reminderEnabled = MutableStateFlow(sp.getBoolean(KEY_ENABLED, false))
    val reminderEnabled: StateFlow<Boolean> = _reminderEnabled

    private val _hour = MutableStateFlow(sp.getInt(KEY_HOUR, 9))
    val hour: StateFlow<Int> = _hour

    private val _minute = MutableStateFlow(sp.getInt(KEY_MINUTE, 0))
    val minute: StateFlow<Int> = _minute

    fun setReminderEnabled(enabled: Boolean) {
        sp.edit().putBoolean(KEY_ENABLED, enabled).apply()
        _reminderEnabled.value = enabled
    }

    fun setReminderTime(hour: Int, minute: Int) {
        sp.edit().putInt(KEY_HOUR, hour).putInt(KEY_MINUTE, minute).apply()
        _hour.value = hour
        _minute.value = minute
    }

    companion object {
        private const val NAME = "muslink_prefs"
        private const val KEY_ENABLED = "reminder_enabled"
        private const val KEY_HOUR = "reminder_hour"
        private const val KEY_MINUTE = "reminder_minute"
    }
}
