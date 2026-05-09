package com.example.gemmasample.domain.usecase

import com.example.gemmasample.domain.model.ModelConfig
import com.example.gemmasample.domain.repository.LlmRepository
import javax.inject.Inject

/**
 * 모델 초기화 UseCase
 * 비즈니스 로직: 설정 유효성 검사 후 초기화
 */
class InitializeModelUseCase @Inject constructor(
    private val llmRepository: LlmRepository
) {
    suspend operator fun invoke(config: ModelConfig): Result<Unit> {
        // 모델 경로 유효성 검사
        if (config.modelPath.isBlank()) {
            return Result.failure(IllegalArgumentException("모델 경로가 비어있습니다."))
        }
        // 파라미터 범위 검사
        if (config.temperature !in 0f..2f) {
            return Result.failure(IllegalArgumentException("Temperature는 0.0~2.0 사이여야 합니다."))
        }
        if (config.topK !in 1..100) {
            return Result.failure(IllegalArgumentException("TopK는 1~100 사이여야 합니다."))
        }
        return llmRepository.initialize(config)
    }
}
