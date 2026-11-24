plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "app.dropletmesh.id.designsystem"
    compileSdk = 37
    defaultConfig { minSdk = 31 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures { compose = true }
}
dependencies {
    // Tous les modules Glasmio : scaffold et preference tirent ui, glass et core.
    api("io.github.neoruaa.glasmio:glasmio-scaffold:0.1.4")
    api("io.github.neoruaa.glasmio:glasmio-preference:0.1.4")
    api("io.github.neoruaa.glasmio:glasmio-icons:0.1.4")
    api(platform("androidx.compose:compose-bom:2026.09.00"))
    api("androidx.compose.material:material-icons-extended")
    testImplementation("junit:junit:4.13.2")
}
