package com.example.gemmasample.data.repository

import com.example.gemmasample.data.datasource.LlmInferenceDataSource
import com.example.gemmasample.domain.model.ChatMessage
import com.example.gemmasample.domain.model.InferenceResult
import com.example.gemmasample.domain.model.ModelConfig
import com.example.gemmasample.domain.model.ModelState
import com.example.gemmasample.domain.repository.LlmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gemma 3n E4B (MediaPipe) LLM Repository 구현체
 *
 * [교체 방법]
 * 1. LlmRepository 인터페이스를 구현하는 새 클래스 작성
 *    예: GeminiApiRepository, OllamaRepository 등
 * 2. AppModule의 @Binds를 새 구현체로 교체
 * 3. 나머지 코드 변경 불필요 (Domain/UI 레이어 독립)
 */
@Singleton
class GemmaLlmRepository @Inject constructor(
    private val dataSource: LlmInferenceDataSource
) : LlmRepository {

    private val _modelState = MutableStateFlow<ModelState>(ModelState.Uninitialized)
    override val modelState: Flow<ModelState> = _modelState.asStateFlow()

    override suspend fun initialize(config: ModelConfig): Result<Unit> {
        _modelState.value = ModelState.Loading
        return try {
            dataSource.initialize(config)
            _modelState.value = ModelState.Ready(config.modelType)
            Result.success(Unit)
        } catch (e: Exception) {
            val errorMsg = e.message ?: "알 수 없는 오류"
            _modelState.value = ModelState.Error(errorMsg)
            Result.failure(e)
        }
    }

    override fun generateResponse(
        prompt: String,
        history: List<ChatMessage>
    ): Flow<InferenceResult> {
        val accumulatedText = StringBuilder()

        return dataSource.generateStreamingResponse(prompt, history)
            .onStart {
                // 스트리밍 시작 알림 (빈 문자열)
            }
            .map { partialText ->
                accumulatedText.append(partialText)
                InferenceResult.Streaming(accumulatedText.toString()) as InferenceResult
            }
            .catch { e ->
                emit(InferenceResult.Error(e))
            }
            // 스트리밍 완료 시 Success 추가 방출
            // (callbackFlow의 close()가 호출되면 Flow가 완료됨)
    }

    override suspend fun close() {
        dataSource.close()
        _modelState.value = ModelState.Uninitialized
    }

    override fun getCurrentConfig(): ModelConfig? = dataSource.getCurrentConfig()
}
