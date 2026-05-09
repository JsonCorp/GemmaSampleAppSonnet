package com.example.gemmasample

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Hilt DI를 위한 Application 클래스
 * @HiltAndroidApp 어노테이션으로 Hilt 컴포넌트 생성 트리거
 */
@HiltAndroidApp
class GemmaSampleApplication : Application()
