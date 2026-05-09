package com.example.gemmasample.domain.model

/**
 * LLM 추론 결과 래퍼
 * 성공/실패 및 스트리밍 상태를 포함
 */
sealed class InferenceResult {
    data class Success(val text: String) : InferenceResult()
    data class Streaming(val partialText: String) : InferenceResult()
    data class Error(val exception: Throwable) : InferenceResult()
}

/**
 * 성능 측정 정보
 */
data class PerformanceMetrics(
    val firstTokenLatencyMs: Long = 0,
    val totalLatencyMs: Long = 0,
    val tokensGenerated: Int = 0,
    val tokensPerSecond: Float = 0f
)
