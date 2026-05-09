package com.example.gemmasample.data.datasource

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// ─── Entity ───────────────────────────────────────────────────────────────────

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "session_id") val sessionId: String,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "role") val role: String,  // "USER" or "MODEL"
    @ColumnInfo(name = "timestamp") val timestamp: Long
)

// ─── DAO ──────────────────────────────────────────────────────────────────────

@Dao
interface ChatMessageDao {

    @Query("SELECT * FROM chat_messages WHERE session_id = :sessionId ORDER BY timestamp ASC")
    fun getMessagesBySession(sessionId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages WHERE session_id = :sessionId")
    suspend fun deleteBySession(sessionId: String)

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAll()
}

// ─── Database ─────────────────────────────────────────────────────────────────

@Database(
    entities = [ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class GemmaSampleDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
}
