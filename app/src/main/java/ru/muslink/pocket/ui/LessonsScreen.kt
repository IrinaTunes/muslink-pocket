package ru.muslink.pocket.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.muslink.pocket.data.LessonEntity
import ru.muslink.pocket.data.StudentEntity
import ru.muslink.pocket.data.toInfo
import ru.muslink.pocket.domain.DateFormat
import ru.muslink.pocket.domain.MoneyFormat
import ru.muslink.pocket.domain.RubleParser

@Composable
fun LessonsScreen(
    students: List<StudentEntity>,
    lessons: List<LessonEntity>,
    onAdd: (Long, Long, Int, Long, Boolean) -> Unit,
    onTogglePaid: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<LessonEntity?>(null) }

    val names = remember(students) { students.associate { it.id to it.name } }
    val grouped = remember(lessons) {
        lessons.sortedByDescending { it.epochDay }.groupBy { it.epochDay }
    }

    ScreenScaffold(
        title = "Занятия",
        fab = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (students.isEmpty()) showAdd = false else showAdd = true
                },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Занятие") }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (students.isEmpty()) {
                EmptyState("Сначала добавьте хотя бы одного ученика на вкладке «Ученики».")
            } else if (lessons.isEmpty()) {
                EmptyState("Занятий пока нет. Нажмите «Занятие», чтобы добавить.")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    grouped.forEach { (day, dayLessons) ->
                        item(key = "header-$day") {
                            DayHeader(
                                day = day,
                                count = dayLessons.size,
                                totalKopecks = dayLessons.sumOf { it.amountKopecks }
                            )
                        }
                        items(dayLessons, key = { "lesson-${it.id}" }) { lesson ->
                            LessonCard(
                                lesson = lesson,
                                studentName = names[lesson.studentId] ?: "Ученик удалён",
                                onTogglePaid = { onTogglePaid(lesson.id, !lesson.paid) },
                                onDelete = { deleting = lesson }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        LessonFormDialog(
            students = students,
            onDismiss = { showAdd = false },
            onAdd = { studentId, day, duration, amount, paid ->
                onAdd(studentId, day, duration, amount, paid)
                showAdd = false
            }
        )
    }

    deleting?.let { lesson ->
        ConfirmDialog(
            title = "Удалить занятие?",
            text = "${names[lesson.studentId] ?: "Ученик"} · ${DateFormat.formatDay(lesson.epochDay)} · " +
                MoneyFormat.formatKopecks(lesson.amountKopecks),
            onConfirm = { onDelete(lesson.id) },
            onDismiss = { deleting = null }
        )
    }
}

@Composable
private fun DayHeader(day: Long, count: Int, totalKopecks: Long) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = DateFormat.formatDay(day),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = "$count · ${MoneyFormat.formatKopecks(totalKopecks)}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LessonCard(
    lesson: LessonEntity,
    studentName: String,
    onTogglePaid: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = studentName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${lesson.durationMin} мин · ${MoneyFormat.formatKopecks(lesson.amountKopecks)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onTogglePaid) {
                Text(
                    text = if (lesson.paid) "Оплачено" else "Долг",
                    color = if (lesson.paid) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LessonFormDialog(
    students: List<StudentEntity>,
    onDismiss: () -> Unit,
    onAdd: (Long, Long, Int, Long, Boolean) -> Unit
) {
    var selectedId by remember { mutableStateOf<Long?>(students.firstOrNull()?.id) }
    var day by remember { mutableStateOf(DateFormat.todayEpochDay()) }
    var durationText by remember { mutableStateOf("60") }
    var amountText by remember { mutableStateOf("") }
    var byRate by remember { mutableStateOf(true) }
    var paid by remember { mutableStateOf(false) }
    var studentMenuOpen by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val selectedStudent = students.firstOrNull { it.id == selectedId }
    val duration = durationText.toIntOrNull() ?: 0
    val computedKopecks = if (selectedStudent != null && duration > 0) {
        selectedStudent.rateKopecks * duration / 60
    } else 0L
    val amount = if (byRate) computedKopecks else (RubleParser.parseToKopecks(amountText) ?: 0L)

    FormDialog(
        title = "Новое занятие",
        onDismiss = onDismiss,
        onConfirm = {
            val sid = selectedId
            if (sid != null && duration > 0) {
                onAdd(sid, day, duration, amount, paid)
            }
        }
    ) {
        Box(Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { studentMenuOpen = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = selectedStudent?.name ?: "Выберите ученика",
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(
                expanded = studentMenuOpen,
                onDismissRequest = { studentMenuOpen = false }
            ) {
                students.forEach { student ->
                    DropdownMenuItem(
                        text = { Text("${student.name} · ${MoneyFormat.formatKopecks(student.rateKopecks)}") },
                        onClick = {
                            selectedId = student.id
                            studentMenuOpen = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.CalendarMonth, contentDescription = null)
            Spacer(Modifier.height(0.dp))
            Text(
                text = "  ${DateFormat.formatDay(day)}",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = durationText,
            onValueChange = { durationText = it.filter { c -> c.isDigit() }.take(3) },
            label = { Text("Длительность, мин") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Считать по ставке", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = if (byRate) "Итого: ${MoneyFormat.formatKopecks(computedKopecks)}"
                    else "Введите сумму вручную",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = byRate, onCheckedChange = { byRate = it })
        }

        if (!byRate) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Сумма, ₽") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(8.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { paid = !paid },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Оплачено сразу", modifier = Modifier.weight(1f))
            Switch(checked = paid, onCheckedChange = { paid = it })
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = day * 86_400_000L
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        day = Math.floorDiv(it, 86_400_000L)
                    }
                    showDatePicker = false
                }) { Text("ОК") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
