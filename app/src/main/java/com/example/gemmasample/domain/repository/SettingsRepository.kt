package com.example.gemmasample.domain.repository

import com.example.gemmasample.domain.model.ModelConfig
import com.example.gemmasample.domain.model.ModelType
import kotlinx.coroutines.flow.Flow

/**
 * 앱 설정 저장소 인터페이스
 * DataStore 구현체와 Domain 레이어를 분리
 */
interface SettingsRepository {
    fun getModelConfig(): Flow<ModelConfig>
    suspend fun saveModelConfig(config: ModelConfig)
    fun getSelectedModelType(): Flow<ModelType>
    suspend fun saveSelectedModelType(modelType: ModelType)
    fun getCustomModelPath(): Flow<String>
    suspend fun saveCustomModelPath(path: String)
}
