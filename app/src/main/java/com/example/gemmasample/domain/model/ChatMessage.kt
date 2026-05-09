package com.example.gemmasample.domain.model

/**
 * 채팅 메시지 도메인 모델
 * UI / Data 레이어와 독립적인 순수 도메인 모델
 */
data class ChatMessage(
    val id: Long = 0,
    val content: String,
    val role: MessageRole,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false  // 스트리밍 응답 중 여부
)

enum class MessageRole {
    USER,
    MODEL
}
