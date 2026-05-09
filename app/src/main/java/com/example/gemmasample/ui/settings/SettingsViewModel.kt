package com.example.gemmasample.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gemmasample.domain.model.ModelConfig
import com.example.gemmasample.domain.model.ModelType
import com.example.gemmasample.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val config = settingsRepository.getModelConfig().first()
            _uiState.update {
                it.copy(
                    selectedModelType = config.modelType,
                    customModelPath = config.modelPath,
                    maxTokens = config.maxTokens,
                    topK = config.topK,
                    temperature = config.temperature,
                    preferGpu = config.preferGpu
                )
            }
        }
    }

    fun onModelTypeSelected(modelType: ModelType) {
        _uiState.update {
            it.copy(
                selectedModelType = modelType,
                customModelPath = if (modelType == ModelType.CUSTOM)
                    it.customModelPath
                else
                    modelType.defaultPath
            )
        }
    }

    fun onCustomPathChanged(path: String) {
        _uiState.update { it.copy(customModelPath = path) }
    }

    fun onMaxTokensChanged(value: Int) {
        _uiState.update { it.copy(maxTokens = value) }
    }

    fun onTopKChanged(value: Int) {
        _uiState.update { it.copy(topK = value) }
    }

    fun onTemperatureChanged(value: Float) {
        _uiState.update { it.copy(temperature = value) }
    }

    fun onPreferGpuChanged(value: Boolean) {
        _uiState.update { it.copy(preferGpu = value) }
    }

    fun saveSettings() {
        viewModelScope.launch {
            val state = _uiState.value
            val config = ModelConfig(
                modelPath = state.customModelPath,
                modelType = state.selectedModelType,
                maxTokens = state.maxTokens,
                topK = state.topK,
                temperature = state.temperature,
                preferGpu = state.preferGpu
            )
            settingsRepository.saveModelConfig(config)
            _uiState.update { it.copy(savedMessage = "설정이 저장되었습니다. 앱을 재시작하거나 채팅 화면에서 모델을 재로드하세요.") }
        }
    }

    fun dismissSavedMessage() {
        _uiState.update { it.copy(savedMessage = null) }
    }
}

data class SettingsUiState(
    val selectedModelType: ModelType = ModelType.GEMMA_3N_E4B,
    val customModelPath: String = ModelType.GEMMA_3N_E4B.defaultPath,
    val maxTokens: Int = 1024,
    val topK: Int = 40,
    val temperature: Float = 0.8f,
    val preferGpu: Boolean = true,
    val savedMessage: String? = null
)
