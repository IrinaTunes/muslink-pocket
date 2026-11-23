package ru.muslink.pocket.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import ru.muslink.pocket.domain.MoneyFormat
import ru.muslink.pocket.domain.RubleParser
import ru.muslink.pocket.domain.Stats

@Composable
fun StudentsScreen(
    students: List<StudentEntity>,
    lessons: List<LessonEntity>,
    onSave: (Long?, String, String, Long, String, String) -> Unit,
    onDelete: (Long) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var showNew by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<StudentEntity?>(null) }
    var deleting by remember { mutableStateOf<StudentEntity?>(null) }

    val filtered = remember(students, query) {
        if (query.isBlank()) students
        else students.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.instrument.contains(query, ignoreCase = true)
        }
    }

    val infos = remember(lessons) { lessons.map { it.toInfo() } }
    val debtsByStudent = remember(infos) {
        infos.filter { !it.paid }.groupBy { it.studentId }.mapValues { (_, list) ->
            list.sumOf { it.amountKopecks }
        }
    }

    ScreenScaffold(
        title = "Ученики",
        fab = {
            ExtendedFloatingActionButton(
                onClick = { showNew = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Ученик") }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                label = { Text("Поиск по имени или инструменту") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true
            )

            if (filtered.isEmpty()) {
                EmptyState(
                    if (students.isEmpty()) "Список пуст. Нажмите «Ученик», чтобы добавить."
                    else "Ничего не найдено."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { student ->
                        StudentCard(
                            student = student,
                            debtKopecks = debtsByStudent[student.id] ?: 0L,
                            onClick = { editing = student },
                            onDelete = { deleting = student }
                        )
                    }
                }
            }
        }
    }

    if (showNew) {
        StudentFormDialog(
            student = null,
            onDismiss = { showNew = false },
            onSave = { name, instrument, rate, phone, note ->
                onSave(null, name, instrument, rate, phone, note)
                showNew = false
            }
        )
    }

    editing?.let { student ->
        StudentFormDialog(
            student = student,
            onDismiss = { editing = null },
            onSave = { name, instrument, rate, phone, note ->
                onSave(student.id, name, instrument, rate, phone, note)
                editing = null
            }
        )
    }

    deleting?.let { student ->
        ConfirmDialog(
            title = "Удалить ученика?",
            text = "«${student.name}» и все его занятия будут удалены без возможности отмены.",
            onConfirm = { onDelete(student.id) },
            onDismiss = { deleting = null }
        )
    }
}

@Composable
private fun StudentCard(
    student: StudentEntity,
    debtKopecks: Long,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier.padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (student.instrument.isNotBlank()) {
                    Text(
                        text = student.instrument,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Ставка: ${MoneyFormat.formatKopecks(student.rateKopecks)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (student.phone.isNotBlank()) {
                    Text(
                        text = student.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (debtKopecks > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Долг: ${MoneyFormat.formatKopecks(debtKopecks)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
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

@Composable
private fun StudentFormDialog(
    student: StudentEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, Long, String, String) -> Unit
) {
    var name by remember { mutableStateOf(student?.name ?: "") }
    var instrument by remember { mutableStateOf(student?.instrument ?: "") }
    var rate by remember {
        mutableStateOf(student?.let { RubleParser.toInputString(it.rateKopecks) } ?: "")
    }
    var phone by remember { mutableStateOf(student?.phone ?: "") }
    var note by remember { mutableStateOf(student?.note ?: "") }

    FormDialog(
        title = if (student == null) "Новый ученик" else "Ученик",
        onDismiss = onDismiss,
        onConfirm = {
            onSave(
                name,
                instrument,
                RubleParser.parseToKopecks(rate) ?: 0L,
                phone,
                note
            )
        }
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Имя") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = instrument,
            onValueChange = { instrument = it },
            label = { Text("Инструмент") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = rate,
            onValueChange = { rate = it },
            label = { Text("Ставка за занятие, ₽") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Телефон") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Заметка") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
