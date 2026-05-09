package com.example.gemmasample.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gemmasample.domain.model.ChatMessage
import com.example.gemmasample.domain.model.InferenceResult
import com.example.gemmasample.domain.model.MessageRole
import com.example.gemmasample.domain.model.ModelState
import com.example.gemmasample.domain.model.PerformanceMetrics
import com.example.gemmasample.domain.repository.LlmRepository
import com.example.gemmasample.domain.repository.SettingsRepository
import com.example.gemmasample.domain.usecase.ClearChatHistoryUseCase
import com.example.gemmasample.domain.usecase.GenerateResponseUseCase
import com.example.gemmasample.domain.usecase.GetChatHistoryUseCase
import com.example.gemmasample.domain.usecase.InitializeModelUseCase
import com.example.gemmasample.domain.usecase.SaveMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * 채팅 화면 ViewModel
 * Android 공식 아키텍처: ViewModel은 UI 상태를 보유하고 UseCase를 호출
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val initializeModelUseCase: InitializeModelUseCase,
    private val generateResponseUseCase: GenerateResponseUseCase,
    private val getChatHistoryUseCase: GetChatHistoryUseCase,
    private val saveMessageUseCase: SaveMessageUseCase,
    private val clearChatHistoryUseCase: ClearChatHistoryUseCase,
    private val llmRepository: LlmRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // 현재 세션 ID (앱 실행마다 새 세션)
    private val sessionId = UUID.randomUUID().toString()

    // ─── UI State ────────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val modelState: StateFlow<ModelState> = llmRepository.modelState
        .stateIn(viewModelScope, SharingStarted.Eagerly, ModelState.Uninitialized)

    private var generationJob: Job? = null

    init {
        // 채팅 히스토리 관찰
        viewModelScope.launch {
            getChatHistoryUseCase(sessionId).collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
        // 앱 시작 시 자동 초기화
        initializeModel()
    }

    // ─── 공개 액션 ───────────────────────────────────────────────────────────

    fun sendMessage(userInput: String) {
        if (userInput.isBlank()) return
        if (modelState.value !is ModelState.Ready) {
            _uiState.update { it.copy(errorMessage = "모델이 준비되지 않았습니다.") }
            return
        }
        if (_uiState.value.isGenerating) {
            stopGeneration()
            return
        }

        viewModelScope.launch {
            // 사용자 메시지 저장 및 UI 추가
            val userMessage = ChatMessage(
                content = userInput,
                role = MessageRole.USER
            )
            saveMessageUseCase(sessionId, userMessage)

            // 스트리밍 응답 메시지 자리 추가
            val streamingMessage = ChatMessage(
                content = "",
                role = MessageRole.MODEL,
                isStreaming = true
            )
            _uiState.update {
                it.copy(
                    isGenerating = true,
                    streamingMessage = streamingMessage,
                    errorMessage = null
                )
            }

            val startTime = System.currentTimeMillis()

            generationJob = launch {
                val history = _uiState.value.messages.takeLast(10)
                generateResponseUseCase(userInput, history).collect { result ->
                    when (result) {
                        is InferenceResult.Streaming -> {
                            _uiState.update { state ->
                                state.copy(
                                    streamingMessage = streamingMessage.copy(
                                        content = result.partialText
                                    )
                                )
                            }
                        }
                        is InferenceResult.Success -> {
                            finalizeResponse(result.text, startTime)
                        }
                        is InferenceResult.Error -> {
                            _uiState.update {
                                it.copy(
                                    isGenerating = false,
                                    streamingMessage = null,
                                    errorMessage = "응답 생성 실패: ${result.exception.message}"
                                )
                            }
                        }
                    }
                }
                // Flow 완료 = 스트리밍 종료
                val finalText = _uiState.value.streamingMessage?.content
                if (!finalText.isNullOrBlank() && _uiState.value.isGenerating) {
                    finalizeResponse(finalText, startTime)
                }
            }
        }
    }

    fun stopGeneration() {
        generationJob?.cancel()
        generationJob = null
        viewModelScope.launch {
            val finalText = _uiState.value.streamingMessage?.content
            if (!finalText.isNullOrBlank()) {
                val modelMessage = ChatMessage(
                    content = finalText + " [중단됨]",
                    role = MessageRole.MODEL
                )
                saveMessageUseCase(sessionId, modelMessage)
            }
            _uiState.update {
                it.copy(
                    isGenerating = false,
                    streamingMessage = null
                )
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            clearChatHistoryUseCase(sessionId)
            _uiState.update { it.copy(messages = emptyList(), streamingMessage = null) }
        }
    }

    fun initializeModel() {
        viewModelScope.launch {
            val config = settingsRepository.getModelConfig().first()
            val result = initializeModelUseCase(config)
            if (result.isFailure) {
                _uiState.update {
                    it.copy(errorMessage = "모델 초기화 실패: ${result.exceptionOrNull()?.message}")
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // ─── Private ─────────────────────────────────────────────────────────────

    private suspend fun finalizeResponse(text: String, startTime: Long) {
        val elapsed = System.currentTimeMillis() - startTime
        val modelMessage = ChatMessage(content = text, role = MessageRole.MODEL)
        saveMessageUseCase(sessionId, modelMessage)

        val metrics = PerformanceMetrics(
            totalLatencyMs = elapsed,
            tokensGenerated = text.split(" ").size,
            tokensPerSecond = if (elapsed > 0) text.split(" ").size / (elapsed / 1000f) else 0f
        )

        _uiState.update {
            it.copy(
                isGenerating = false,
                streamingMessage = null,
                lastMetrics = metrics
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch { llmRepository.close() }
    }
}

// ─── UI State Data Class ──────────────────────────────────────────────────────

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val streamingMessage: ChatMessage? = null,
    val errorMessage: String? = null,
    val lastMetrics: PerformanceMetrics? = null
) {
    /** UI에 표시할 전체 메시지 목록 (완료 메시지 + 현재 스트리밍 메시지) */
    val displayMessages: List<ChatMessage>
        get() = if (streamingMessage != null) messages + streamingMessage else messages
}
