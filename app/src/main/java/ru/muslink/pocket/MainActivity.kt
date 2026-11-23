package ru.muslink.pocket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.muslink.pocket.ui.AppViewModel
import ru.muslink.pocket.ui.MuslinkRoot
import ru.muslink.pocket.ui.MuslinkTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MuslinkTheme {
                val vm: AppViewModel = viewModel()
                val students by vm.students.collectAsState()
                val lessons by vm.lessons.collectAsState()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MuslinkRoot(
                        vm = vm,
                        students = students,
                        lessons = lessons
                    )
                }
            }
        }
    }
}
