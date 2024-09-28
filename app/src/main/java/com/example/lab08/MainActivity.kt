package com.example.lab08

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.Room
import kotlinx.coroutines.launch
import com.example.lab08.ui.theme.Lab08Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab08Theme {
                val db = Room.databaseBuilder(
                    applicationContext,
                    TaskDatabase::class.java,
                    "task_db"
                ).build()

                val taskDao = db.taskDao()
                val viewModel = TaskViewModel(taskDao)

                TaskScreen(viewModel)
            }
        }
    }
}

@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var newTaskDescription by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var showCompleted by remember { mutableStateOf(false) }
    var sortByName by remember { mutableStateOf(false) }
    var sortByCompletion by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Campo para agregar una nueva tarea
        TextField(
            value = newTaskDescription,
            onValueChange = { newTaskDescription = it },
            label = { Text("Nueva tarea") },
            modifier = Modifier.fillMaxWidth()
        )

        // Botón para agregar la tarea
        Button(
            onClick = {
                if (newTaskDescription.isNotEmpty()) {
                    viewModel.addTask(newTaskDescription)
                    newTaskDescription = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Agregar tarea")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de búsqueda
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar tarea") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        // Botones de filtro para mostrar tareas pendientes o completadas
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { showCompleted = false }) {
                Text("Mostrar Pendientes")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { showCompleted = true }) {
                Text("Mostrar Completadas")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botones para ordenar las tareas por nombre o estado
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { sortByName = true; sortByCompletion = false }) {
                Text("Ordenar por Nombre")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { sortByCompletion = true; sortByName = false }) {
                Text("Ordenar por Estado")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filtrar las tareas según el estado (completadas/pendientes) y el texto de búsqueda
        val filteredTasks = if (searchQuery.isNotEmpty()) {
            viewModel.searchTasks(searchQuery)
        } else if (showCompleted) {
            viewModel.getFilteredTasks(true)
        } else {
            viewModel.getFilteredTasks(false)
        }

        // Ordenar las tareas según la opción seleccionada (nombre o estado)
        val sortedTasks = if (sortByName) {
            filteredTasks.sortedBy { it.description }
        } else if (sortByCompletion) {
            filteredTasks.sortedBy { it.isCompleted }
        } else {
            filteredTasks
        }

        // Mostrar lista de tareas con las opciones de editar y eliminar
        sortedTasks.forEach { task ->
            var isEditing by remember { mutableStateOf(false) }
            var editedDescription by remember { mutableStateOf(task.description) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Si estamos editando, mostrar un TextField para editar la descripción
                if (isEditing) {
                    TextField(
                        value = editedDescription,
                        onValueChange = { editedDescription = it },
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            viewModel.editTask(task, editedDescription)
                            isEditing = false
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Guardar")
                    }
                } else {
                    // Mostrar la descripción si no estamos editando
                    Text(
                        text = task.description,
                        modifier = Modifier.weight(1f)
                    )
                    Row {
                        Button(
                            onClick = { isEditing = true },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text("Editar", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = { viewModel.toggleTaskCompletion(task) },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                if (task.isCompleted) "Completada" else "Pendiente",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        Button(
                            onClick = { viewModel.deleteTask(task) },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text("Eliminar", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Botón para eliminar todas las tareas
        Button(
            onClick = { coroutineScope.launch { viewModel.deleteAllTasks() } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Eliminar todas las tareas")
        }
    }
}
@Preview(showBackground = true)
@Composable
fun TaskScreenPreview() {
    Lab08Theme {
        // Aquí podrías simular el `viewModel` para ver cómo se muestra
    }
}
