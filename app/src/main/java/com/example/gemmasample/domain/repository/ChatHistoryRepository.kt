package com.example.gemmasample.domain.repository

import com.example.gemmasample.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

/**
 * 채팅 히스토리 저장소 인터페이스
 * Room DB 구현체와 Domain 레이어를 분리
 */
interface ChatHistoryRepository {

    /**
     * 세션의 모든 메시지를 Flow로 관찰
     * @param sessionId 대화 세션 ID
     */
    fun getMessages(sessionId: String): Flow<List<ChatMessage>>

    /**
     * 메시지 저장
     */
    suspend fun saveMessage(sessionId: String, message: ChatMessage): Long

    /**
     * 세션의 모든 메시지 삭제
     */
    suspend fun clearSession(sessionId: String)

    /**
     * 전체 히스토리 삭제
     */
    suspend fun clearAll()
}
