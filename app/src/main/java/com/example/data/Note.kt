package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val colorIndex: Int = 0, // Maps to a soft pastel background theme
    val category: String = "الكل", // Default "All" in Arabic or customized
    val isPinned: Boolean = false
)
