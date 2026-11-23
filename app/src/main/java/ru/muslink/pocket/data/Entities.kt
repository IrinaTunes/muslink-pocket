package ru.muslink.pocket.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.muslink.pocket.domain.LessonInfo

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val instrument: String,
    val rateKopecks: Long,
    val phone: String = "",
    val note: String = ""
)

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val studentId: Long,
    val epochDay: Long,
    val durationMin: Int,
    val amountKopecks: Long,
    val paid: Boolean = false
)

fun LessonEntity.toInfo(): LessonInfo = LessonInfo(
    id = id,
    studentId = studentId,
    epochDay = epochDay,
    durationMin = durationMin,
    amountKopecks = amountKopecks,
    paid = paid
)
