plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

// Release version + signing come from environment (CI) with local-safe defaults.
// No env set → versionCode 1 / versionName "1.0", release unsigned (local behavior unchanged).
val versionCodeProp: Int =
    (System.getenv("VERSION_CODE") ?: project.findProperty("VERSION_CODE")?.toString())?.toIntOrNull() ?: 1
val versionNameProp: String =
    System.getenv("VERSION_NAME") ?: project.findProperty("VERSION_NAME")?.toString() ?: "1.0"
val releaseStoreFile: String? =
    System.getenv("ANDROID_KEYSTORE_FILE") ?: project.findProperty("ANDROID_KEYSTORE_FILE")?.toString()
val releaseStorePassword: String? =
    System.getenv("ANDROID_KEYSTORE_PASSWORD") ?: project.findProperty("ANDROID_KEYSTORE_PASSWORD")?.toString()
val releaseKeyAlias: String? =
    System.getenv("ANDROID_KEY_ALIAS") ?: project.findProperty("ANDROID_KEY_ALIAS")?.toString()
val releaseKeyPassword: String? =
    System.getenv("ANDROID_KEY_PASSWORD") ?: project.findProperty("ANDROID_KEY_PASSWORD")?.toString()

android {
    namespace = "com.dinsdiary"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dinsdiary"
        minSdk = 26
        targetSdk = 36
        versionCode = versionCodeProp
        versionName = versionNameProp

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        // Only registered when CI provides a keystore; otherwise release stays unsigned.
        if (!releaseStoreFile.isNullOrBlank()) {
            create("release") {
                storeFile = file(releaseStoreFile)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Signed only when the keystore file actually exists (CI restores it pre-build).
            if (!releaseStoreFile.isNullOrBlank() && file(releaseStoreFile).exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.workmanager)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.hilt.work)
    implementation(libs.hilt.navigation.compose)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidxtest.junit)
    androidTestImplementation(libs.androidxtest.core)
}
