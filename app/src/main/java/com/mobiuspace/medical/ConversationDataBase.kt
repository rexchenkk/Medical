package com.mobiuspace.medical

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ConversationModel::class], version = 1)
abstract class ConversationDataBase: RoomDatabase() {
  abstract fun conversationDao(): ConversationDao
}