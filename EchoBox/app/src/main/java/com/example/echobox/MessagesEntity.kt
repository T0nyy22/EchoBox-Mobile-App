package com.example.echobox

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessagesEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val message: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val targetedDate: Long,
    val image: String? = null,
    val isUnlocked: Boolean = false,
    val category: String = MessageCategory.PERSONAL
)