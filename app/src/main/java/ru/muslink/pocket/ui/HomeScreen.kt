package ru.muslink.pocket.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.muslink.pocket.data.LessonEntity
import ru.muslink.pocket.data.StudentEntity
import ru.muslink.pocket.data.toInfo
import ru.muslink.pocket.domain.DateFormat
import ru.muslink.pocket.domain.MoneyFormat
import ru.muslink.pocket.domain.Stats

@Composable
fun HomeScreen(
    students: List<StudentEntity>,
    lessons: List<LessonEntity>
) {
    val today = remember { DateFormat.todayEpochDay() }
    val infos = remember(lessons) { lessons.map { it.toInfo() } }
    val month = Stats.monthOf(today)

    val income = Stats.incomeForMonth(infos, month)
    val paid = Stats.paidForMonth(infos, month)
    val debtTotal = Stats.totalDebt(infos)
    val activeStudents = Stats.activeStudentIds(infos, today).size
    val upcoming = Stats.upcoming(infos, today, 5)
    val names = remember(students) { students.associate { it.id to it.name } }

    ScreenScaffold(title = "Главная") { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = DateFormat.formatMonth(month),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Доход за месяц",
                    value = MoneyFormat.formatKopecks(income),
                    modifier = Modifier.weight(1f),
                    note = "оплачено ${MoneyFormat.formatKopecks(paid)}"
                )
                StatCard(
                    title = "Долги",
                    value = MoneyFormat.formatKopecks(debtTotal),
                    modifier = Modifier.weight(1f),
                    note = if (debtTotal > 0) "есть неоплаченные" else "всё оплачено",
                    accent = if (debtTotal > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Активных учеников",
                    value = activeStudents.toString(),
                    modifier = Modifier.weight(1f),
                    note = "всего в базе ${students.size}"
                )
                StatCard(
                    title = "Долг за месяц",
                    value = MoneyFormat.formatKopecks(Stats.debtForMonth(infos, month)),
                    modifier = Modifier.weight(1f),
                    note = "начислено ${MoneyFormat.formatKopecks(income)}"
                )
            }
            Spacer(Modifier.height(16.dp))

            SectionCard(title = "Ближайшие занятия") {
                if (upcoming.isEmpty()) {
                    EmptyState("Занятий пока нет. Добавьте их на вкладке «Занятия».")
                } else {
                    upcoming.forEachIndexed { index, lesson ->
                        if (index > 0) HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = names[lesson.studentId] ?: "Ученик удалён",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${DateFormat.formatDay(lesson.epochDay)} · ${lesson.durationMin} мин",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = MoneyFormat.formatKopecks(lesson.amountKopecks),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (lesson.paid) "оплачено" else "долг",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (lesson.paid) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            SectionCard(title = "Итоги месяца") {
                LabelValueRow("Начислено", MoneyFormat.formatKopecks(income))
                Spacer(Modifier.height(6.dp))
                LabelValueRow("Оплачено", MoneyFormat.formatKopecks(paid))
                Spacer(Modifier.height(6.dp))
                LabelValueRow("Осталось получить", MoneyFormat.formatKopecks(income - paid))
                Spacer(Modifier.height(6.dp))
                LabelValueRow(
                    "Занятий за месяц",
                    infos.count { Stats.monthOf(it.epochDay) == month }.toString()
                )
            }
            Spacer(Modifier.height(24.dp))
            Spacer(Modifier.size(8.dp))
        }
    }
}
