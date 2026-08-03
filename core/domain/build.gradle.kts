plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.musicplayer.core.domain"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        // 纯 Kotlin 领域层，无 Android UI 依赖
        compose = false
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // 仅依赖协程核心（纯 JVM，保持 domain 零 Android 依赖）
    implementation(libs.kotlinx.coroutines.core)
    // JSR-330 注入注解（纯 JVM，无 Android 依赖；UseCase 的 @Inject 需要此来源）
    implementation("javax.inject:javax.inject:1")
}
