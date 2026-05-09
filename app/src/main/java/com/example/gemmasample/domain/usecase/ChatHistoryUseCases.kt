package com.example.gemmasample.domain.usecase

import com.example.gemmasample.domain.model.ChatMessage
import com.example.gemmasample.domain.repository.ChatHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 채팅 히스토리 조회 UseCase
 */
class GetChatHistoryUseCase @Inject constructor(
    private val chatHistoryRepository: ChatHistoryRepository
) {
    operator fun invoke(sessionId: String): Flow<List<ChatMessage>> =
        chatHistoryRepository.getMessages(sessionId)
}

/**
 * 채팅 히스토리 저장 UseCase
 */
class SaveMessageUseCase @Inject constructor(
    private val chatHistoryRepository: ChatHistoryRepository
) {
    suspend operator fun invoke(sessionId: String, message: ChatMessage): Long =
        chatHistoryRepository.saveMessage(sessionId, message)
}

/**
 * 채팅 히스토리 삭제 UseCase
 */
class ClearChatHistoryUseCase @Inject constructor(
    private val chatHistoryRepository: ChatHistoryRepository
) {
    suspend operator fun invoke(sessionId: String) =
        chatHistoryRepository.clearSession(sessionId)
}
