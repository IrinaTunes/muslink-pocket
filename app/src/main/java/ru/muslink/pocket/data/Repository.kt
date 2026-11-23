package ru.muslink.pocket.data

import kotlinx.coroutines.flow.Flow

class Repository(private val db: AppDatabase) {

    private val students = db.studentDao()
    private val lessons = db.lessonDao()

    fun observeStudents(): Flow<List<StudentEntity>> = students.observeAll()
    fun observeLessons(): Flow<List<LessonEntity>> = lessons.observeAll()

    suspend fun addStudent(student: StudentEntity): Long = students.insert(student)
    suspend fun updateStudent(student: StudentEntity) = students.update(student)
    suspend fun deleteStudent(id: Long) {
        lessons.deleteForStudent(id)
        students.deleteById(id)
    }

    suspend fun addLesson(lesson: LessonEntity): Long = lessons.insert(lesson)
    suspend fun setLessonPaid(id: Long, paid: Boolean) = lessons.setPaid(id, paid)
    suspend fun deleteLesson(id: Long) = lessons.deleteById(id)
    suspend fun lessonsForDay(epochDay: Long): List<LessonEntity> = lessons.forDay(epochDay)
}
