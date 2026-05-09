package com.example.gemmasample.data.datasource

import android.content.Context
import com.example.gemmasample.domain.model.ChatMessage
import com.example.gemmasample.domain.model.MessageRole
import com.example.gemmasample.domain.model.ModelConfig
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * MediaPipe LLM Inference API를 직접 사용하는 DataSource
 *
 * [주요 역할]
 * - LlmInference 인스턴스 라이프사이클 관리
 * - Gemma 3n E4B 모델 로딩 및 추론
 * - Gemma 채팅 프롬프트 포맷 처리
 * - 스트리밍 응답을 Flow로 변환
 *
 * [프롬프트 포맷 - Gemma instruction-tuned]
 * <start_of_turn>user
 * {user_message}<end_of_turn>
 * <start_of_turn>model
 * {model_response}<end_of_turn>
 */
@Singleton
class LlmInferenceDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var llmInference: LlmInference? = null
    private var currentConfig: ModelConfig? = null

    /**
     * 모델 초기화
     * 기존 인스턴스가 있으면 해제 후 재생성
     */
    suspend fun initialize(config: ModelConfig): Unit =
        suspendCancellableCoroutine { continuation ->
            try {
                // 기존 인스턴스 해제
                llmInference?.close()
                llmInference = null

                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(config.modelPath)
                    .setMaxTopK(config.topK)
                    .setMaxTokens(config.maxTokens)
                    .apply {
                        // GPU 가속 설정 (Android only)
                        if (config.preferGpu) {
                            setPreferredBackend(LlmInference.Backend.GPU)
                        } else {
                            setPreferredBackend(LlmInference.Backend.CPU)
                        }
                    }
                    .build()

                llmInference = LlmInference.createFromOptions(context, options)
                currentConfig = config
                continuation.resume(Unit)
            } catch (e: Exception) {
                continuation.resumeWithException(
                    RuntimeException("모델 초기화 실패: ${e.message}", e)
                )
            }
        }

    /**
     * 스트리밍 응답 생성
     * @param prompt 사용자 입력
     * @param history 이전 대화 히스토리
     */
    fun generateStreamingResponse(
        prompt: String,
        history: List<ChatMessage>
    ): Flow<String> = callbackFlow {
        val inference = llmInference
            ?: throw IllegalStateException("모델이 초기화되지 않았습니다. initialize()를 먼저 호출하세요.")

        // Gemma instruction-tuned 프롬프트 포맷 구성
        val fullPrompt = buildGemmaPrompt(prompt, history)

        try {
            inference.generateResponseAsync(
                fullPrompt,
                // 부분 결과 콜백 (스트리밍)
                { partialResult, done ->
                    if (!partialResult.isNullOrEmpty()) {
                        trySend(partialResult)
                    }
                    if (done) {
                        close()
                    }
                }
            )
        } catch (e: Exception) {
            close(e)
        }

        awaitClose { /* 채널 종료 시 정리 작업 없음 */ }
    }

    /**
     * Gemma instruction-tuned 모델의 채팅 프롬프트 포맷 생성
     *
     * 형식:
     * <start_of_turn>user
     * {message}<end_of_turn>
     * <start_of_turn>model
     * {response}<end_of_turn>
     * ...
     * <start_of_turn>user
     * {current_prompt}<end_of_turn>
     * <start_of_turn>model
     */
    private fun buildGemmaPrompt(
        currentPrompt: String,
        history: List<ChatMessage>
    ): String = buildString {
        // 이전 대화 히스토리 추가
        history.forEach { message ->
            when (message.role) {
                MessageRole.USER -> {
                    append("<start_of_turn>user\n")
                    append(message.content)
                    append("<end_of_turn>\n")
                }
                MessageRole.MODEL -> {
                    append("<start_of_turn>model\n")
                    append(message.content)
                    append("<end_of_turn>\n")
                }
            }
        }
        // 현재 사용자 입력
        append("<start_of_turn>user\n")
        append(currentPrompt)
        append("<end_of_turn>\n")
        // 모델 응답 시작 토큰
        append("<start_of_turn>model\n")
    }

    /**
     * 현재 설정 반환
     */
    fun getCurrentConfig(): ModelConfig? = currentConfig

    /**
     * 리소스 해제
     */
    fun close() {
        llmInference?.close()
        llmInference = null
        currentConfig = null
    }
}
