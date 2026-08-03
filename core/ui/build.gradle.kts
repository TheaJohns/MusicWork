plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.musicplayer.core.ui"
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // 仅 UI 基础依赖，无任何业务/Android 框架强依赖
    implementation(libs.androidx.core.ktx)
    // 领域模型（Song / SongSource）：core:ui 的列表项直接依赖，必须显式声明模块依赖
    implementation(project(":core:domain"))
    implementation(platform(libs.androidx.compose.bom))
    // Compose 基础布局库（Row/Column/Modifier.weight/clickable 等），显式声明以确保 API 可见
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // 图标资源（MusicNote / CloudOff 等来源标签与错误视图用，来自 material-icons-extended）
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // 图片加载（Coil，带占位/错误图）
    implementation(libs.coil.compose)
}
