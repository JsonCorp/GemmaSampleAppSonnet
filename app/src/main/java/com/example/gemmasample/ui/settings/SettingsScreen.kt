package com.example.gemmasample.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gemmasample.domain.model.ModelType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.savedMessage) {
        uiState.savedMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissSavedMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("모델 설정") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Button(
                onClick = viewModel::saveSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Text(text = "  설정 저장", style = MaterialTheme.typography.titleMedium)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── 모델 선택 ────────────────────────────────────────────────────
            SectionTitle("모델 선택")
            Text(
                text = "기기 성능에 맞는 모델을 선택하세요.\nE4B: 플래그십 폰 권장 (8GB+ RAM)\nE2B: 미드레인지 폰 (6GB RAM)\n1B: 저사양 기기",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            ModelType.entries.forEach { modelType ->
                FilterChip(
                    selected = uiState.selectedModelType == modelType,
                    onClick = { viewModel.onModelTypeSelected(modelType) },
                    label = {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = modelType.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = modelType.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // ── 모델 경로 ────────────────────────────────────────────────────
            SectionTitle("모델 파일 경로")
            Text(
                text = "ADB로 기기에 푸시한 .task 파일 경로\nadb push model.task /data/local/tmp/llm/",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = uiState.customModelPath,
                onValueChange = viewModel::onCustomPathChanged,
                label = { Text("모델 경로") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // ── 추론 파라미터 ─────────────────────────────────────────────────
            SectionTitle("추론 파라미터")

            // Temperature
            ParameterSlider(
                label = "Temperature",
                value = uiState.temperature,
                valueText = "%.1f".format(uiState.temperature),
                valueRange = 0f..2f,
                onValueChange = viewModel::onTemperatureChanged,
                description = "높을수록 창의적, 낮을수록 정확"
            )

            // TopK
            ParameterSlider(
                label = "Top-K",
                value = uiState.topK.toFloat(),
                valueText = uiState.topK.toString(),
                valueRange = 1f..100f,
                onValueChange = { viewModel.onTopKChanged(it.toInt()) },
                description = "다음 토큰 후보 수"
            )

            // MaxTokens
            ParameterSlider(
                label = "Max Tokens",
                value = uiState.maxTokens.toFloat(),
                valueText = uiState.maxTokens.toString(),
                valueRange = 128f..2048f,
                onValueChange = { viewModel.onMaxTokensChanged(it.toInt()) },
                description = "최대 생성 토큰 수"
            )

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // ── 하드웨어 가속 ──────────────────────────────────────────────────
            SectionTitle("하드웨어 가속")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GPU 가속 사용",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "GPU를 사용하면 추론 속도가 빨라집니다\n(GPU 미지원 기기에서는 자동으로 CPU 사용)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.preferGpu,
                    onCheckedChange = viewModel::onPreferGpuChanged
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // ── 모델 교체 안내 ────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "🔧 개발자 안내: 모델 교체",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "다른 LLM으로 교체하려면:\n" +
                                "1. LlmRepository 인터페이스를 구현\n" +
                                "2. AppModule의 @Binds 구현체 교체\n" +
                                "3. UI/Domain 코드 변경 불필요",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(80.dp)) // 바텀 버튼 공간
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun ParameterSlider(
    label: String,
    value: Float,
    valueText: String,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    description: String
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = description,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
