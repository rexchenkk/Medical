package com.mobiuspace.medical

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ConversationDao {
  @Query("Select * from conversation")
  fun getAllConversation(): List<ConversationModel>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun addConversation(conversationModel: ConversationModel)
}