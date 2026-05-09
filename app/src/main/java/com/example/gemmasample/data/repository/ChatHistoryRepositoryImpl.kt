package com.example.gemmasample.data.repository

import com.example.gemmasample.data.datasource.ChatMessageDao
import com.example.gemmasample.data.datasource.ChatMessageEntity
import com.example.gemmasample.domain.model.ChatMessage
import com.example.gemmasample.domain.model.MessageRole
import com.example.gemmasample.domain.repository.ChatHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room DB 기반 ChatHistoryRepository 구현체
 */
@Singleton
class ChatHistoryRepositoryImpl @Inject constructor(
    private val dao: ChatMessageDao
) : ChatHistoryRepository {

    override fun getMessages(sessionId: String): Flow<List<ChatMessage>> =
        dao.getMessagesBySession(sessionId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun saveMessage(sessionId: String, message: ChatMessage): Long =
        dao.insertMessage(message.toEntity(sessionId))

    override suspend fun clearSession(sessionId: String) =
        dao.deleteBySession(sessionId)

    override suspend fun clearAll() = dao.deleteAll()

    // ─── 매퍼 ────────────────────────────────────────────────────────────────

    private fun ChatMessageEntity.toDomain() = ChatMessage(
        id = id,
        content = content,
        role = MessageRole.valueOf(role),
        timestamp = timestamp
    )

    private fun ChatMessage.toEntity(sessionId: String) = ChatMessageEntity(
        id = id,
        sessionId = sessionId,
        content = content,
        role = role.name,
        timestamp = timestamp
    )
}
