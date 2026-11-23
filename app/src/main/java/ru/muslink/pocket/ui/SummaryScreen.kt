package ru.muslink.pocket.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.muslink.pocket.data.LessonEntity
import ru.muslink.pocket.data.StudentEntity
import ru.muslink.pocket.data.toInfo
import ru.muslink.pocket.domain.DateFormat
import ru.muslink.pocket.domain.MoneyFormat
import ru.muslink.pocket.domain.MonthSummary
import ru.muslink.pocket.domain.Stats

@Composable
fun SummaryScreen(
    students: List<StudentEntity>,
    lessons: List<LessonEntity>
) {
    val context = LocalContext.current
    val infos = remember(lessons) { lessons.map { it.toInfo() } }
    val summaries = remember(infos) { Stats.monthlySummaries(infos) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            val csv = CsvExport.build(students, lessons)
            val result = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(csv.toByteArray(Charsets.UTF_8))
                } ?: error("Не удалось открыть файл")
            }
            Toast.makeText(
                context,
                if (result.isSuccess) "CSV сохранён" else "Ошибка сохранения файла",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    ScreenScaffold(title = "Итоги") { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Экспорт в CSV",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Все занятия: дата, ученик, длительность, сумма, статус. Файл можно открыть в Excel или Google Таблицах.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { launcher.launch("muslink-lessons.csv") },
                        enabled = lessons.isNotEmpty()
                    ) {
                        Icon(Icons.Filled.FileDownload, contentDescription = null)
                        Spacer(Modifier.height(0.dp))
                        Text("  Сохранить CSV")
                    }
                }
            }

            if (summaries.isEmpty()) {
                EmptyState("Пока нет данных для итогов.")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(Modifier.fillMaxWidth()) {
                            Text(
                                "Всего начислено",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                MoneyFormat.formatKopecks(infos.sumOf { it.amountKopecks }),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    items(summaries) { summary ->
                        MonthCard(
                            summary = summary,
                            lessonsCount = infos.count { Stats.monthOf(it.epochDay) == summary.ym }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthCard(summary: MonthSummary, lessonsCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Text(
                    text = DateFormat.formatMonth(summary.ym),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = MoneyFormat.formatKopecks(summary.accruedKopecks),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "$lessonsCount зан.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(Modifier.height(10.dp))
            LabelValueRow("Оплачено", MoneyFormat.formatKopecks(summary.paidKopecks))
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth()) {
                Text(
                    "Долг",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = MoneyFormat.formatKopecks(summary.debtKopecks),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (summary.debtKopecks > 0) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
