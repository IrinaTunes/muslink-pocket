package ru.muslink.pocket.ui

import ru.muslink.pocket.data.LessonEntity
import ru.muslink.pocket.data.StudentEntity
import ru.muslink.pocket.domain.DateFormat
import ru.muslink.pocket.domain.MoneyFormat

object CsvExport {

    private const val SEP = ";"

    /**
     * CSV для Excel: UTF-8 BOM, разделитель «;».
     */
    fun build(students: List<StudentEntity>, lessons: List<LessonEntity>): String {
        val names = students.associate { it.id to it.name }
        val sb = StringBuilder()
        sb.append('\uFEFF')
        sb.append(listOf("Дата", "Ученик", "Инструмент", "Длительность (мин)", "Сумма (руб)", "Статус").joinToString(SEP))
        sb.append("\r\n")

        val instruments = students.associate { it.id to it.instrument }

        lessons.sortedWith(compareBy({ it.epochDay }, { it.id })).forEach { lesson ->
            val row = listOf(
                DateFormat.formatDay(lesson.epochDay),
                escape(names[lesson.studentId] ?: "Ученик удалён"),
                escape(instruments[lesson.studentId] ?: ""),
                lesson.durationMin.toString(),
                MoneyFormat.formatAmount(lesson.amountKopecks).replace('\u00A0', ' '),
                if (lesson.paid) "Оплачено" else "Долг"
            )
            sb.append(row.joinToString(SEP))
            sb.append("\r\n")
        }
        return sb.toString()
    }

    private fun escape(value: String): String {
        val needsQuotes = value.contains(SEP) || value.contains('"') || value.contains('\n')
        return if (needsQuotes) "\"" + value.replace("\"", "\"\"") + "\"" else value
    }
}
