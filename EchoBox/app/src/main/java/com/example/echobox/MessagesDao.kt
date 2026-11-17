package com.example.echobox

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MessagesDao {

    @Insert
    suspend fun insertMessage(message: MessagesEntity)

    @Query("SELECT * FROM messages ORDER BY createdAt ASC")
    suspend fun getAllMessages() : List<MessagesEntity>

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessageById(id: Int): MessagesEntity?

    @Query("SELECT * FROM messages WHERE targetedDate > :now ORDER BY createdAt ASC")
    suspend fun getLockedMessages(now: Long): List<MessagesEntity>

    @Query("SELECT * FROM messages WHERE targetedDate <= :now ORDER BY createdAt ASC")
    suspend fun getLockedMessages(now: Long): List<MessagesEntity>

    @Update
    suspend fun updateMessage(message: MessagesEntity)

    @Delete
    suspend fun deleteMessage(message: MessagesEntity)
}