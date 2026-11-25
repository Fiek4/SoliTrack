plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)            // Necesario para que Room genere su código
}

android {
    namespace = "com.example.solitrack"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.solitrack"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    // Los tests unitarios corren sobre la plataforma JUnit 5 (necesaria para Kotest)
    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Íconos extendidos (Error, PhotoCamera, Category, etc.)
    implementation(libs.androidx.material.icons.extended)

    // Navegación entre pantallas
    implementation(libs.androidx.navigation.compose)

    // ViewModel para Compose (viewModel(), viewModelFactory)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Room: persistencia local
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Coil: mostrar la foto de evidencia desde su Uri
    implementation(libs.coil.compose)

    // Retrofit + Gson: consumir la API REST del catálogo de servicios
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // Corrutinas para las llamadas a la API en segundo plano
    implementation(libs.kotlinx.coroutines.android)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")




    // ---------- Testing unitario (Guía 15) ----------
    testImplementation(libs.junit)                    // JUnit 4 (tests existentes)
    testImplementation(libs.junit.jupiter)            // JUnit 5
    testRuntimeOnly(libs.junit.vintage.engine)        // Ejecuta los tests JUnit 4 sobre JUnit 5
    testImplementation(libs.kotest.runner.junit5)     // Kotest: estilo StringSpec
    testImplementation(libs.kotest.assertions.core)   // shouldBe, shouldContain, ...
    testImplementation(libs.mockk)                    // Objetos simulados (mocks)
    testImplementation(libs.kotlinx.coroutines.test)  // Probar corrutinas y viewModelScope

    // ---------- Testing de UI en el emulador ----------
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}