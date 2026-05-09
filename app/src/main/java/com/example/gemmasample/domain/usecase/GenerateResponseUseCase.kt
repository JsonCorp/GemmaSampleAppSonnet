package com.example.gemmasample.domain.usecase

import com.example.gemmasample.domain.model.ChatMessage
import com.example.gemmasample.domain.model.InferenceResult
import com.example.gemmasample.domain.repository.LlmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * AI 응답 생성 UseCase
 * 비즈니스 로직: 입력 검증 및 히스토리 관리
 */
class GenerateResponseUseCase @Inject constructor(
    private val llmRepository: LlmRepository
) {
    operator fun invoke(
        userInput: String,
        history: List<ChatMessage> = emptyList(),
        maxHistorySize: Int = 10
    ): Flow<InferenceResult> {
        if (userInput.isBlank()) {
            return flow {
                emit(InferenceResult.Error(IllegalArgumentException("입력이 비어있습니다.")))
            }
        }

        // 히스토리 크기 제한 (컨텍스트 윈도우 관리)
        val trimmedHistory = if (history.size > maxHistorySize) {
            history.takeLast(maxHistorySize)
        } else {
            history
        }

        return llmRepository.generateResponse(
            prompt = userInput.trim(),
            history = trimmedHistory
        )
    }
}
