package ru.muslink.pocket.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.muslink.pocket.BuildConfig
import java.util.Locale

@Composable
fun SettingsScreen(vm: AppViewModel) {
    val enabled by vm.reminderEnabled.collectAsStateWithLifecycle()
    val hour by vm.reminderHour.collectAsStateWithLifecycle()
    val minute by vm.reminderMinute.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    ScreenScaffold(title = "Настройки") { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            SectionCard(title = "Напоминание о занятиях") {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            val next = !enabled
                            if (next && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            vm.setReminderEnabled(next)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Ежедневное напоминание", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "Одно уведомление в день о занятиях на сегодня. Если занятий нет — уведомление не придёт.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(checked = enabled, onCheckedChange = { next ->
                        if (next && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        vm.setReminderEnabled(next)
                    })
                }

                if (enabled) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Время напоминания",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TimeDropdown(
                            value = hour,
                            range = 0..23,
                            onSelect = { vm.setReminderTime(it, minute) }
                        )
                        Text(" :", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.width(8.dp))
                        TimeDropdown(
                            value = minute,
                            range = 0..59,
                            step = 5,
                            onSelect = { vm.setReminderTime(hour, it) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            SectionCard(title = "Данные") {
                Text(
                    "Все записи хранятся только на этом устройстве в локальной базе Room. " +
                        "Приложение не использует интернет и не передаёт данные куда-либо.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))

            SectionCard(title = "О приложении") {
                LabelValueRow("Название", "Muslink Pocket")
                Spacer(Modifier.height(6.dp))
                LabelValueRow("Версия", BuildConfig.VERSION_NAME)
                Spacer(Modifier.height(6.dp))
                LabelValueRow("Назначение", "Учёт учеников и занятий")
                Spacer(Modifier.height(10.dp))
                Text(
                    "Суммы хранятся в копейках (целые числа), поэтому расчёты не теряют точность.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "Сделано для преподавателя музыкальной школы",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TimeDropdown(
    value: Int,
    range: IntRange,
    step: Int = 1,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(String.format(Locale.US, "%02d", value))
            Spacer(Modifier.width(4.dp))
            androidx.compose.material3.Icon(
                Icons.Filled.ArrowDropDown,
                contentDescription = null
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            range.filter { it % step == 0 }.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            String.format(Locale.US, "%02d", item),
                            fontWeight = if (item == value) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onSelect(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
