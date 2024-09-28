package com.example.lab08

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel(private val dao: TaskDao) : ViewModel() {

    // Estado para la lista de tareas
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    init {
        // Al inicializar, cargamos las tareas de la base de datos
        viewModelScope.launch {
            _tasks.value = dao.getAllTasks()
        }
    }

    // Función para añadir una nueva tarea
    fun addTask(description: String) {
        val newTask = Task(description = description)
        viewModelScope.launch {
            dao.insertTask(newTask)
            _tasks.value = dao.getAllTasks() // Recargamos la lista
        }
    }

    // Función para alternar el estado de completado de una tarea
    fun toggleTaskCompletion(task: Task) {
        val updatedTask = task.copy(isCompleted = !task.isCompleted)
        viewModelScope.launch {
            dao.updateTask(updatedTask)  // Actualizar la tarea en la base de datos
            _tasks.value = dao.getAllTasks()  // Volver a cargar todas las tareas para reflejar los cambios
        }
    }


    // Función para eliminar todas las tareas
    fun deleteAllTasks() {
        viewModelScope.launch {
            dao.deleteAllTasks()
            _tasks.value = emptyList() // Vaciamos la lista en el estado
        }
    }
    //Eliminar las tareas uno por uno
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            dao.deleteTask(task)
            _tasks.value = dao.getAllTasks() // Actualizamos la lista
        }
    }
    //Actualizar la tarea
    fun editTask(task: Task, newDescription: String) {
        val updatedTask = task.copy(description = newDescription)
        viewModelScope.launch {
            dao.updateTask(updatedTask)
            _tasks.value = dao.getAllTasks() // Recargamos la lista
        }
    }
    //Filtrar tareas
    fun getFilteredTasks(showCompleted: Boolean): List<Task> {
        return if (showCompleted) {
            _tasks.value.filter { it.isCompleted }
        } else {
            _tasks.value.filter { !it.isCompleted }
        }
    }
    //Buscar tareas
    fun searchTasks(query: String): List<Task> {
        return _tasks.value.filter { it.description.contains(query, ignoreCase = true) }
    }
    //Ordenar tareas
    fun getSortedTasks(byName: Boolean, byCompletion: Boolean): List<Task> {
        return when {
            byName -> _tasks.value.sortedBy { it.description }
            byCompletion -> _tasks.value.sortedBy { it.isCompleted }
            else -> _tasks.value
        }
    }

}
