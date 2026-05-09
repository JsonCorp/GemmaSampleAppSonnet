package com.example.gemmasample.core.di

import android.content.Context
import androidx.room.Room
import com.example.gemmasample.data.datasource.ChatMessageDao
import com.example.gemmasample.data.datasource.GemmaSampleDatabase
import com.example.gemmasample.data.repository.ChatHistoryRepositoryImpl
import com.example.gemmasample.data.repository.GemmaLlmRepository
import com.example.gemmasample.data.repository.SettingsRepositoryImpl
import com.example.gemmasample.domain.repository.ChatHistoryRepository
import com.example.gemmasample.domain.repository.LlmRepository
import com.example.gemmasample.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt DI 모듈 - 데이터베이스 & Repository 바인딩
 *
 * [모델 교체 방법]
 * LlmRepository의 @Binds를 변경하여 구현체를 교체할 수 있습니다.
 *
 * 예) Gemma → Gemini API로 교체:
 *   @Binds
 *   abstract fun bindLlmRepository(impl: GeminiApiRepository): LlmRepository
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * LlmRepository 바인딩
     * 이 줄만 변경하면 전체 모델 구현체가 교체됩니다
     */
    @Binds
    @Singleton
    abstract fun bindLlmRepository(
        impl: GemmaLlmRepository
    ): LlmRepository

    @Binds
    @Singleton
    abstract fun bindChatHistoryRepository(
        impl: ChatHistoryRepositoryImpl
    ): ChatHistoryRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): GemmaSampleDatabase = Room.databaseBuilder(
        context,
        GemmaSampleDatabase::class.java,
        "gemma_sample.db"
    ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideChatMessageDao(database: GemmaSampleDatabase): ChatMessageDao =
        database.chatMessageDao()
}
