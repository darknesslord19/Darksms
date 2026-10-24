package com.darknes.sms.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    // ---- Konusma listesi ----
    // Sadece istenen gizlilik durumundaki konusmalar gelir:
    // ana ekran hidden=0, kasa ekrani hidden=1 cagirir.
    @Query("SELECT * FROM threads WHERE hidden = :hidden AND archived = 0 ORDER BY timestamp DESC")
    fun threads(hidden: Boolean): Flow<List<ThreadEntity>>

    @Query("SELECT * FROM threads WHERE address = :address LIMIT 1")
    suspend fun thread(address: String): ThreadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertThread(thread: ThreadEntity)

    @Query("UPDATE threads SET hidden = :hidden WHERE address = :address")
    suspend fun setHidden(address: String, hidden: Boolean)

    @Query("UPDATE threads SET unread = 0 WHERE address = :address")
    suspend fun clearUnread(address: String)

    // ---- Mesajlar ----
    @Query("SELECT * FROM messages WHERE threadAddress = :address ORDER BY timestamp ASC")
    fun messages(address: String): Flow<List<MessageEntity>>

    @Insert
    suspend fun insertMessage(msg: MessageEntity): Long

    @Update
    suspend fun updateMessage(msg: MessageEntity)

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    suspend fun message(id: Long): MessageEntity?

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessage(id: Long)

    @Query("DELETE FROM messages WHERE threadAddress = :address")
    suspend fun deleteThreadMessages(address: String)

    @Query("DELETE FROM threads WHERE address = :address")
    suspend fun deleteThread(address: String)

    // Arama
    @Query("SELECT * FROM messages WHERE body LIKE '%' || :q || '%' ORDER BY timestamp DESC")
    fun search(q: String): Flow<List<MessageEntity>>
}
