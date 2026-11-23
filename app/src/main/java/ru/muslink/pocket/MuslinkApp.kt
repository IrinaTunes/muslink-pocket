package ru.muslink.pocket

import android.app.Application
import ru.muslink.pocket.data.Prefs
import ru.muslink.pocket.work.Notifications
import ru.muslink.pocket.work.ReminderScheduler

class MuslinkApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Notifications.createChannel(this)

        val prefs = Prefs(this)
        if (prefs.reminderEnabled.value) {
            ReminderScheduler.schedule(this, prefs.hour.value, prefs.minute.value)
        }
    }
}
