package ru.muslink.pocket.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Query("SELECT * FROM students ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun byId(id: Long): StudentEntity?

    @Query("SELECT * FROM students ORDER BY name COLLATE NOCASE ASC")
    suspend fun all(): List<StudentEntity>

    @Insert
    suspend fun insert(student: StudentEntity): Long

    @Update
    suspend fun update(student: StudentEntity)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface LessonDao {

    @Query("SELECT * FROM lessons ORDER BY epochDay DESC, id DESC")
    fun observeAll(): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE epochDay = :epochDay ORDER BY id ASC")
    suspend fun forDay(epochDay: Long): List<LessonEntity>

    @Insert
    suspend fun insert(lesson: LessonEntity): Long

    @Update
    suspend fun update(lesson: LessonEntity)

    @Query("UPDATE lessons SET paid = :paid WHERE id = :id")
    suspend fun setPaid(id: Long, paid: Boolean)

    @Query("DELETE FROM lessons WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM lessons WHERE studentId = :studentId")
    suspend fun deleteForStudent(studentId: Long)
}
