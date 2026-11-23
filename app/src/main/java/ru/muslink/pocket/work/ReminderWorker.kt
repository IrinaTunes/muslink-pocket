package ru.muslink.pocket.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ru.muslink.pocket.data.AppDatabase
import java.time.LocalDate

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now().toEpochDay()
        val db = AppDatabase.get(applicationContext)
        val lessonsToday = db.lessonDao().forDay(today)
        if (lessonsToday.isEmpty()) {
            // Нет занятий — уведомление не показываем.
            return Result.success()
        }
        val names = db.studentDao().all().associate { it.id to it.name }
        val ordered = lessonsToday.map { names[it.studentId] ?: "Ученик" }
        Notifications.showTodayLessons(applicationContext, ordered)
        return Result.success()
    }
}
