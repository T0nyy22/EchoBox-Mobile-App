package com.example.echobox

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [MessagesEntity::class], version = 1)
abstract class MessagesDatabase : RoomDatabase() {
    abstract fun messagesDao(): MessagesDao

    companion object {
        @Volatile
        private var INSTANCE: MessagesDatabase? = null

        fun getInstance(context: Context): MessagesDatabase {
            if (INSTANCE == null){
                synchronized(MessagesDatabase::class){
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        MessagesDatabase::class.java,
                        name="message_db"
                    ).build()
                }
            }
            return INSTANCE!!
        }
    }
}
