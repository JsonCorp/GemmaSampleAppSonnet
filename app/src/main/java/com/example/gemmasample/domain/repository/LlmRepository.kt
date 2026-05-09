package com.example.gemmasample.domain.repository

import com.example.gemmasample.domain.model.ChatMessage
import com.example.gemmasample.domain.model.InferenceResult
import com.example.gemmasample.domain.model.ModelConfig
import com.example.gemmasample.domain.model.ModelState
import kotlinx.coroutines.flow.Flow

/**
 * LLM 추론 Repository 인터페이스
 *
 * 이 인터페이스를 통해 모델 구현체를 교체할 수 있습니다.
 * - GemmaLlmRepository: MediaPipe + Gemma 3n E4B
 * - GeminiApiRepository: Gemini REST API
 * - MockLlmRepository: 테스트용 Mock
 *
 * Clean Architecture의 핵심: Domain 레이어는 구체적인 구현을 모름
 */
interface LlmRepository {

    /**
     * 현재 모델 상태를 관찰하는 Flow
     */
    val modelState: Flow<ModelState>

    /**
     * 모델 초기화
     * @param config 모델 설정
     */
    suspend fun initialize(config: ModelConfig): Result<Unit>

    /**
     * 텍스트 생성 (스트리밍)
     * @param prompt 입력 프롬프트
     * @param history 이전 대화 히스토리
     * @return InferenceResult Flow (Streaming -> Success or Error)
     */
    fun generateResponse(
        prompt: String,
        history: List<ChatMessage> = emptyList()
    ): Flow<InferenceResult>

    /**
     * 모델 리소스 해제
     */
    suspend fun close()

    /**
     * 현재 로드된 모델 설정 반환
     */
    fun getCurrentConfig(): ModelConfig?
}
