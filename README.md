# Gemma 4 E4B Android Sample App

Gemma 3n E4B (Gemma 4 Edge) 모델을 Android에서 온디바이스로 실행하는 **샘플 앱**입니다.
영어 학습 앱, 운동 앱 등 LLM 기반 앱 개발의 참고용 보일러플레이트로 활용할 수 있습니다.

---

## 📐 아키텍처 구조

```
app/
└── java/com/example/gemmasample/
    ├── core/
    │   └── di/
    │       └── AppModule.kt          ← Hilt DI 모듈 (모델 교체 포인트)
    │
    ├── domain/                       ← 순수 Kotlin, Android 의존성 없음
    │   ├── model/
    │   │   ├── ChatMessage.kt
    │   │   ├── ModelConfig.kt        ← ModelType enum (지원 모델 목록)
    │   │   └── InferenceResult.kt
    │   ├── repository/
    │   │   ├── LlmRepository.kt      ← ★ 모델 교체 인터페이스
    │   │   ├── ChatHistoryRepository.kt
    │   │   └── SettingsRepository.kt
    │   └── usecase/
    │       ├── InitializeModelUseCase.kt
    │       ├── GenerateResponseUseCase.kt
    │       └── ChatHistoryUseCases.kt
    │
    ├── data/                         ← MediaPipe, Room, DataStore 구현
    │   ├── datasource/
    │   │   ├── LlmInferenceDataSource.kt  ← MediaPipe LLM Inference API
    │   │   └── ChatDatabase.kt            ← Room DB
    │   └── repository/
    │       ├── GemmaLlmRepository.kt      ← LlmRepository 구현체
    │       ├── ChatHistoryRepositoryImpl.kt
    │       └── SettingsRepositoryImpl.kt
    │
    └── ui/                           ← Jetpack Compose UI
        ├── chat/
        │   ├── ChatViewModel.kt
        │   └── ChatScreen.kt
        ├── settings/
        │   ├── SettingsViewModel.kt
        │   └── SettingsScreen.kt
        ├── theme/
        │   └── Theme.kt
        └── GemmaSampleNavHost.kt
```

---

## ⚙️ 모델 준비 (필수)

### 1. Hugging Face에서 모델 다운로드

```
https://huggingface.co/litert-community/Gemma3n-E4B-it-int4
```

- 파일: `gemma-3n-E4B-it-int4.task` (약 3.2GB)
- 라이선스 동의 후 다운로드 가능

### 2. ADB로 기기에 전송

```bash
# 기존 모델 디렉토리 초기화
adb shell rm -rf /data/local/tmp/llm/
adb shell mkdir -p /data/local/tmp/llm/

# 모델 푸시 (기기 연결 후)
adb push gemma-3n-E4B-it-int4.task /data/local/tmp/llm/gemma3_4b.task
```

### 3. 기기 요구사항

| 모델 | 최소 RAM | 권장 기기 |
|------|----------|-----------|
| E4B (4B) | 8GB | Pixel 8 Pro, Galaxy S24+ 이상 |
| E2B (2B) | 6GB | Pixel 7, Galaxy S22 이상 |
| 1B | 4GB | 대부분의 2022년 이후 기기 |

---

## 🔄 모델 교체 방법

### 다른 LLM으로 교체 (예: Gemini Nano, 커스텀 모델)

**1단계: 새 Repository 구현**

```kotlin
// data/repository/GeminiNanoRepository.kt
@Singleton
class GeminiNanoRepository @Inject constructor(
    // ... 필요한 의존성
) : LlmRepository {  // ← 이 인터페이스만 구현하면 됨
    
    override val modelState: Flow<ModelState> = ...
    
    override suspend fun initialize(config: ModelConfig): Result<Unit> { ... }
    
    override fun generateResponse(
        prompt: String,
        history: List<ChatMessage>
    ): Flow<InferenceResult> { ... }
    
    override suspend fun close() { ... }
    
    override fun getCurrentConfig(): ModelConfig? { ... }
}
```

**2단계: DI 모듈 바인딩 변경**

```kotlin
// core/di/AppModule.kt
@Binds
@Singleton
abstract fun bindLlmRepository(
    impl: GeminiNanoRepository  // ← 이 줄만 변경
): LlmRepository
```

**3단계 완료**: UI / Domain 코드 변경 불필요 ✅

---

## 🏗️ 사용 기술 스택

| 분류 | 기술 |
|------|------|
| 언어 | Kotlin |
| UI | Jetpack Compose + Material3 |
| 아키텍처 | MVVM + Clean Architecture (ui/domain/data) |
| DI | Hilt |
| 비동기 | Kotlin Coroutines + Flow |
| LLM 추론 | MediaPipe LLM Inference API |
| 로컬 DB | Room |
| 설정 저장 | DataStore Preferences |
| 내비게이션 | Navigation Compose |

---

## 🚀 주요 기능

- **온디바이스 LLM 추론**: 네트워크 없이 완전한 오프라인 동작
- **스트리밍 응답**: 토큰 단위 실시간 출력 (Flow 기반)
- **다중 모델 지원**: E4B / E2B / 1B / Custom 모델 전환
- **GPU/CPU 선택**: 기기 사양에 따른 백엔드 선택
- **대화 히스토리**: Room DB 영구 저장
- **성능 측정**: 응답 속도 및 tok/s 표시
- **추론 파라미터 조정**: Temperature, Top-K, Max Tokens 실시간 설정

---

## 📱 향후 앱 개발 시 활용

이 샘플의 구조를 그대로 유지하면서 UseCase만 추가하면 됩니다:

```kotlin
// 영어 학습 앱 예시
class EnglishCorrectionUseCase @Inject constructor(
    private val llmRepository: LlmRepository
) {
    fun correctSentence(sentence: String): Flow<InferenceResult> =
        llmRepository.generateResponse(
            prompt = "Correct this English sentence and explain: $sentence"
        )
}

// 운동 앱 예시
class WorkoutPlanUseCase @Inject constructor(
    private val llmRepository: LlmRepository
) {
    fun generatePlan(goal: String, level: String): Flow<InferenceResult> =
        llmRepository.generateResponse(
            prompt = "Create a $level workout plan for: $goal"
        )
}
```

---

## 📄 라이선스

Gemma 모델: [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0)
