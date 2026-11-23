package ru.muslink.pocket.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.muslink.pocket.data.LessonEntity
import ru.muslink.pocket.data.StudentEntity

private data class Dest(val route: String, val label: String, val icon: ImageVector)

private val destinations = listOf(
    Dest("home", "Главная", Icons.Filled.Home),
    Dest("students", "Ученики", Icons.Filled.Person),
    Dest("lessons", "Занятия", Icons.Filled.DateRange),
    Dest("summary", "Итоги", Icons.Filled.Assessment),
    Dest("settings", "Настройки", Icons.Filled.Settings)
)

@Composable
fun MuslinkRoot(
    vm: AppViewModel,
    students: List<StudentEntity>,
    lessons: List<LessonEntity>
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: "home"

    Scaffold(
        bottomBar = {
            NavigationBar {
                destinations.forEach { dest ->
                    NavigationBarItem(
                        selected = currentRoute == dest.route,
                        onClick = {
                            if (currentRoute != dest.route) {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            composable("home") {
                HomeScreen(students = students, lessons = lessons)
            }
            composable("students") {
                StudentsScreen(
                    students = students,
                    lessons = lessons,
                    onSave = { id, name, instrument, rate, phone, note ->
                        vm.saveStudent(id, name, instrument, rate, phone, note)
                    },
                    onDelete = { vm.deleteStudent(it) }
                )
            }
            composable("lessons") {
                LessonsScreen(
                    students = students,
                    lessons = lessons,
                    onAdd = { studentId, day, duration, amount, paid ->
                        vm.addLesson(studentId, day, duration, amount, paid)
                    },
                    onTogglePaid = { id, paid -> vm.setLessonPaid(id, paid) },
                    onDelete = { vm.deleteLesson(it) }
                )
            }
            composable("summary") {
                SummaryScreen(students = students, lessons = lessons)
            }
            composable("settings") {
                SettingsScreen(vm = vm)
            }
        }
    }
}
