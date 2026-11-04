package com.example.echobox

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [MessagesEntity::class], version = 2)
abstract class MessagesDatabase : RoomDatabase() {
    abstract fun messagesDao(): MessagesDao

    companion object {
        @Volatile
        private var INSTANCE: MessagesDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE messages ADD COLUMN category TEXT NOT NULL DEFAULT 'Personal'"
                )
            }
        }
        fun getInstance(context: Context): MessagesDatabase {
            if (INSTANCE == null){
                synchronized(MessagesDatabase::class){
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        MessagesDatabase::class.java,
                        name="message_db"
                    ).addMigrations(MIGRATION_1_2).build()
                }
            }
            return INSTANCE!!
        }
    }
}
