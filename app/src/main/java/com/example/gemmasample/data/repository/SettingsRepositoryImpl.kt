package com.example.gemmasample.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.gemmasample.domain.model.ModelConfig
import com.example.gemmasample.domain.model.ModelType
import com.example.gemmasample.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "gemma_settings"
)

/**
 * DataStore 기반 SettingsRepository 구현체
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    // ─── Preference Keys ─────────────────────────────────────────────────────
    private object Keys {
        val MODEL_TYPE = stringPreferencesKey("model_type")
        val MODEL_PATH = stringPreferencesKey("model_path")
        val MAX_TOKENS = intPreferencesKey("max_tokens")
        val TOP_K = intPreferencesKey("top_k")
        val TEMPERATURE = floatPreferencesKey("temperature")
        val PREFER_GPU = booleanPreferencesKey("prefer_gpu")
        val CUSTOM_MODEL_PATH = stringPreferencesKey("custom_model_path")
    }

    override fun getModelConfig(): Flow<ModelConfig> =
        context.dataStore.data.map { prefs ->
            val modelType = ModelType.valueOf(
                prefs[Keys.MODEL_TYPE] ?: ModelType.GEMMA_3N_E4B.name
            )
            ModelConfig(
                modelPath = prefs[Keys.MODEL_PATH] ?: modelType.defaultPath,
                modelType = modelType,
                maxTokens = prefs[Keys.MAX_TOKENS] ?: 1024,
                topK = prefs[Keys.TOP_K] ?: 40,
                temperature = prefs[Keys.TEMPERATURE] ?: 0.8f,
                preferGpu = prefs[Keys.PREFER_GPU] ?: true
            )
        }

    override suspend fun saveModelConfig(config: ModelConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.MODEL_TYPE] = config.modelType.name
            prefs[Keys.MODEL_PATH] = config.modelPath
            prefs[Keys.MAX_TOKENS] = config.maxTokens
            prefs[Keys.TOP_K] = config.topK
            prefs[Keys.TEMPERATURE] = config.temperature
            prefs[Keys.PREFER_GPU] = config.preferGpu
        }
    }

    override fun getSelectedModelType(): Flow<ModelType> =
        context.dataStore.data.map { prefs ->
            ModelType.valueOf(prefs[Keys.MODEL_TYPE] ?: ModelType.GEMMA_3N_E4B.name)
        }

    override suspend fun saveSelectedModelType(modelType: ModelType) {
        context.dataStore.edit { prefs ->
            prefs[Keys.MODEL_TYPE] = modelType.name
            prefs[Keys.MODEL_PATH] = modelType.defaultPath
        }
    }

    override fun getCustomModelPath(): Flow<String> =
        context.dataStore.data.map { prefs ->
            prefs[Keys.CUSTOM_MODEL_PATH] ?: ""
        }

    override suspend fun saveCustomModelPath(path: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CUSTOM_MODEL_PATH] = path
        }
    }
}
