plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "app.dropletmesh.id"
    compileSdk = 37
    defaultConfig {
        applicationId = "app.dropletmesh.id"
        // Glasmio : Android 12 (API 31) minimum ; la réfraction complète du
        // verre demande l'API 33, en dessous le verre est simplifié.
        minSdk = 31
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
}
dependencies {
    implementation(project(":core:designsystem"))
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    // Déverrouillage (empreinte, visage ou code de l'appareil).
    implementation("androidx.biometric:biometric:1.1.0")
    // La graine Ed25519 de l'appareil, chiffrée par une clé du Keystore.
    implementation("com.google.crypto.tink:tink-android:1.15.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // Le scanner : CameraX + zxing (sans services Google Play).
    implementation("androidx.camera:camera-camera2:1.4.2")
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    implementation("androidx.camera:camera-view:1.4.2")
    implementation("com.google.zxing:core:3.5.3")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
}
