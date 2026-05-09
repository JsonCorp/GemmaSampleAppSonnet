package com.example.gemmasample.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gemmasample.domain.model.ChatMessage
import com.example.gemmasample.domain.model.MessageRole
import com.example.gemmasample.domain.model.ModelState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val modelState by viewModel.modelState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    // 새 메시지 시 자동 스크롤
    LaunchedEffect(uiState.displayMessages.size) {
        if (uiState.displayMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.displayMessages.lastIndex)
        }
    }

    // 에러 Snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Gemma Sample",
                            style = MaterialTheme.typography.titleMedium
                        )
                        ModelStateIndicator(modelState)
                    }
                },
                actions = {
                    // 히스토리 삭제
                    IconButton(onClick = viewModel::clearHistory) {
                        Icon(Icons.Default.Delete, contentDescription = "히스토리 삭제")
                    }
                    // 설정
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "설정")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            ChatInputBar(
                isGenerating = uiState.isGenerating,
                modelReady = modelState is ModelState.Ready,
                onSend = viewModel::sendMessage,
                onStop = viewModel::stopGeneration
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.displayMessages.isEmpty()) {
                // 빈 상태 안내
                EmptyStateView(
                    modelState = modelState,
                    onRetry = viewModel::initializeModel
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.displayMessages,
                        key = { "${it.role}_${it.timestamp}_${it.id}" }
                    ) { message ->
                        ChatBubble(message = message)
                    }

                    // 성능 정보 표시
                    uiState.lastMetrics?.let { metrics ->
                        item {
                            PerformanceInfo(
                                totalMs = metrics.totalLatencyMs,
                                tokensPerSec = metrics.tokensPerSecond
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Sub-Composables ─────────────────────────────────────────────────────────

@Composable
private fun ModelStateIndicator(state: ModelState) {
    val (text, color) = when (state) {
        ModelState.Uninitialized -> "모델 미로드" to Color.Gray
        ModelState.Loading -> "모델 로딩 중..." to MaterialTheme.colorScheme.tertiary
        is ModelState.Ready -> state.modelType.displayName to MaterialTheme.colorScheme.primary
        is ModelState.Error -> "오류: ${state.message.take(30)}" to MaterialTheme.colorScheme.error
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (state is ModelState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(10.dp),
                strokeWidth = 1.5.dp,
                color = color
            )
            Spacer(Modifier.width(4.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == MessageRole.USER
    val bubbleColor = if (isUser)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surface

    val textColor = if (isUser)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSurface

    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault())
        .format(Date(message.timestamp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (!isUser) {
            Text(
                text = "🤖 Gemma",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }

        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(
                topStart = if (isUser) 16.dp else 4.dp,
                topEnd = if (isUser) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = message.content,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = if (!isUser) FontFamily.Monospace else FontFamily.Default
                )
                // 스트리밍 커서 애니메이션
                if (message.isStreaming) {
                    StreamingCursor()
                }
            }
        }

        Text(
            text = timeStr,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun StreamingCursor() {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )
    Text(
        text = "▋",
        color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun ChatInputBar(
    isGenerating: Boolean,
    modelReady: Boolean,
    onSend: (String) -> Unit,
    onStop: () -> Unit
) {
    var input by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = if (modelReady) "메시지를 입력하세요..." else "모델 로딩 대기 중...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                enabled = modelReady && !isGenerating,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (input.isNotBlank()) {
                            onSend(input)
                            input = ""
                        }
                    }
                ),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(Modifier.width(8.dp))

            // 전송 / 중지 버튼
            IconButton(
                onClick = {
                    if (isGenerating) {
                        onStop()
                    } else if (input.isNotBlank()) {
                        onSend(input)
                        input = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isGenerating) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
            ) {
                Icon(
                    imageVector = if (isGenerating) Icons.Default.Stop else Icons.Default.Send,
                    contentDescription = if (isGenerating) "중지" else "전송",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun EmptyStateView(
    modelState: ModelState,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🤖", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Gemma 4 E4B Sample",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))

        when (modelState) {
            ModelState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                Text(
                    text = "모델을 로딩 중입니다...\n(최초 실행 시 수 분 소요)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            is ModelState.Ready -> {
                Text(
                    text = "대화를 시작해보세요!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            is ModelState.Error -> {
                Text(
                    text = "모델 로드 실패\n모델 파일이 기기에 있는지 확인하세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                androidx.compose.material3.Button(onClick = onRetry) {
                    Text("다시 시도")
                }
            }
            else -> {
                Text(
                    text = "설정에서 모델 경로를 확인하세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PerformanceInfo(totalMs: Long, tokensPerSec: Float) {
    AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
        Text(
            text = "⚡ ${totalMs}ms | %.1f tok/s".format(tokensPerSec),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
