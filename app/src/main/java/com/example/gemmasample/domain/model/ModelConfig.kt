package com.example.gemmasample.domain.model

/**
 * LLM 모델 설정 도메인 모델
 * 모델 교체가 가능하도록 추상화된 설정값
 */
data class ModelConfig(
    val modelPath: String,
    val modelType: ModelType = ModelType.GEMMA_3N_E4B,
    val maxTokens: Int = 1024,
    val topK: Int = 40,
    val temperature: Float = 0.8f,
    val randomSeed: Int = 101,
    val preferGpu: Boolean = true
)

/**
 * 지원 모델 타입
 * 새 모델 추가 시 이 enum에 항목 추가 후 팩토리에서 처리
 */
enum class ModelType(
    val displayName: String,
    val defaultPath: String,
    val description: String
) {
    GEMMA_3N_E4B(
        displayName = "Gemma 3n E4B (4B)",
        defaultPath = "/data/local/tmp/llm/gemma3_4b.task",
        description = "Gemma-3n Effective 4B - 고성능 온디바이스 모델"
    ),
    GEMMA_3N_E2B(
        displayName = "Gemma 3n E2B (2B)",
        defaultPath = "/data/local/tmp/llm/gemma3_2b.task",
        description = "Gemma-3n Effective 2B - 미드레인지 기기용 경량 모델"
    ),
    GEMMA_3_1B(
        displayName = "Gemma 3 1B",
        defaultPath = "/data/local/tmp/llm/gemma3_1b.task",
        description = "Gemma 3 1B - 초경량 온디바이스 모델"
    ),
    CUSTOM(
        displayName = "Custom Model",
        defaultPath = "/data/local/tmp/llm/custom.task",
        description = "사용자 정의 .task 모델"
    )
}

/**
 * LLM 초기화 상태
 */
sealed class ModelState {
    object Uninitialized : ModelState()
    object Loading : ModelState()
    data class Ready(val modelType: ModelType) : ModelState()
    data class Error(val message: String) : ModelState()
}
