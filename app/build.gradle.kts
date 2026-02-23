import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.3.0"
}

android {
    namespace = "is.hi.present"
    compileSdk {
        version = release(36)
    }

    val localProperties = Properties().apply {
        load(rootDir.resolve("local.properties").inputStream())
    }

    val url = localProperties.getProperty("SUPABASE_URL") ?: ""
    val key = localProperties.getProperty("SUPABASE_PUBLISHABLE_KEY") ?: ""

    defaultConfig {
        applicationId = "is.hi.present"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SUPABASE_URL", "\"${url}\"")
        buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", "\"${key}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.androidx.room.common.jvm)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    implementation(platform(libs.bom))
    implementation(libs.supabase.kt)
    implementation(libs.auth.kt)
    implementation(libs.postgrest.kt)
    implementation(libs.storage.kt)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation("androidx.compose.material:material-icons-extended")
}