package com.example.data.model

data class TaskItem(
    val id: String,
    val text: String,
    val completed: Boolean = false
)
