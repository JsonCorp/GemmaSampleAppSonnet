package com.example.gemmasample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.gemmasample.ui.GemmaSampleNavHost
import com.example.gemmasample.ui.theme.GemmaSampleTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Hilt를 사용하는 MainActivity
 * @AndroidEntryPoint 어노테이션으로 Hilt 의존성 주입 활성화
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GemmaSampleTheme {
                GemmaSampleNavHost()
            }
        }
    }
}
