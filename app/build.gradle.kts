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
    // Seulement la langue de l'app (le français) : les traductions des
    // bibliothèques dans 80 langues ne sont pas embarquées.
    androidResources { localeFilters += listOf("fr") }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    // La clé de signature de publication n'est JAMAIS dans le dépôt. Si la CI
    // la reçoit (secrets GitHub, voir README), elle signe avec ; sinon la
    // version release est signée avec la clé de debug : installable pour
    // essayer, pas publiable sur Google Play.
    val cleRelease = System.getenv("DROPLET_ID_KEYSTORE")?.let(::file)?.takeIf { it.exists() }
    signingConfigs {
        if (cleRelease != null) {
            create("publication") {
                storeFile = cleRelease
                storePassword = System.getenv("DROPLET_ID_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("DROPLET_ID_KEY_ALIAS")
                keyPassword = System.getenv("DROPLET_ID_KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        release {
            // R8 : code et ressources inutilisés retirés, le reste optimisé.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = if (cleRelease != null) signingConfigs.getByName("publication") else signingConfigs.getByName("debug")
        }
    }
    // Une APK par processeur (la bibliothèque de verre embarque du code natif) :
    // chaque téléphone ne télécharge que la sienne.
    splits {
        abi {
            // Pas pour l'AAB : Google Play fait lui-même le découpage.
            isEnable = gradle.startParameter.taskNames.none { it.contains("bundle", ignoreCase = true) }
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86_64")
            isUniversalApk = false
        }
    }
    packaging {
        resources.excludes += setOf("META-INF/*.version", "META-INF/**/LICENSE*", "META-INF/AL2.0", "META-INF/LGPL2.1", "kotlin/**", "DebugProbesKt.bin")
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
