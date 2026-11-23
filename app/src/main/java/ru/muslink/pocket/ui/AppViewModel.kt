package ru.muslink.pocket.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.muslink.pocket.data.AppDatabase
import ru.muslink.pocket.data.LessonEntity
import ru.muslink.pocket.data.Prefs
import ru.muslink.pocket.data.Repository
import ru.muslink.pocket.data.StudentEntity
import ru.muslink.pocket.work.ReminderScheduler

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = Repository(AppDatabase.get(application))
    private val prefs = Prefs(application)

    val students: StateFlow<List<StudentEntity>> = repo.observeStudents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val lessons: StateFlow<List<LessonEntity>> = repo.observeLessons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val reminderEnabled: StateFlow<Boolean> = prefs.reminderEnabled
    val reminderHour: StateFlow<Int> = prefs.hour
    val reminderMinute: StateFlow<Int> = prefs.minute

    private val _lastExport = MutableStateFlow<String?>(null)
    val lastExport: StateFlow<String?> = _lastExport

    fun notifyExport(message: String) {
        _lastExport.value = message
    }

    fun clearExportMessage() {
        _lastExport.value = null
    }

    fun saveStudent(
        id: Long?,
        name: String,
        instrument: String,
        rateKopecks: Long,
        phone: String,
        note: String
    ) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return
        viewModelScope.launch {
            val entity = StudentEntity(
                id = id ?: 0L,
                name = cleanName,
                instrument = instrument.trim(),
                rateKopecks = rateKopecks,
                phone = phone.trim(),
                note = note.trim()
            )
            if (id == null) repo.addStudent(entity) else repo.updateStudent(entity)
        }
    }

    fun deleteStudent(id: Long) {
        viewModelScope.launch { repo.deleteStudent(id) }
    }

    fun addLesson(
        studentId: Long,
        epochDay: Long,
        durationMin: Int,
        amountKopecks: Long,
        paid: Boolean
    ) {
        viewModelScope.launch {
            repo.addLesson(
                LessonEntity(
                    studentId = studentId,
                    epochDay = epochDay,
                    durationMin = durationMin,
                    amountKopecks = amountKopecks,
                    paid = paid
                )
            )
        }
    }

    fun setLessonPaid(id: Long, paid: Boolean) {
        viewModelScope.launch { repo.setLessonPaid(id, paid) }
    }

    fun deleteLesson(id: Long) {
        viewModelScope.launch { repo.deleteLesson(id) }
    }

    fun setReminderEnabled(enabled: Boolean) {
        prefs.setReminderEnabled(enabled)
        reschedule()
    }

    fun setReminderTime(hour: Int, minute: Int) {
        prefs.setReminderTime(hour, minute)
        reschedule()
    }

    private fun reschedule() {
        val app = getApplication<Application>()
        if (prefs.reminderEnabled.value) {
            ReminderScheduler.schedule(app, prefs.hour.value, prefs.minute.value)
        } else {
            ReminderScheduler.cancel(app)
        }
    }
}
