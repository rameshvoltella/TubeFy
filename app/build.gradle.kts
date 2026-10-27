import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Firebase Analytics: google-services.json is kept out of git, so builds without it (other people's
// clones, F-Droid) still work; analytics then simply stays off
// (Release and "TubeFy Debug" are separate apps in Firebase, so testing doesn't mix into the real numbers.)
// One line on purpose: F-Droid's build recipe deletes the lines mentioning google-services / firebase.
if (file("google-services.json").exists()) apply(plugin = "com.google.gms.google-services")

// Release signing: the key's location and passwords live in keystore.properties (never in git).
// Without that file (F-Droid, other clones) release builds are simply left unsigned.
val keystoreProperties = Properties().apply {
    rootProject.file("keystore.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

android {
    namespace = "com.ramzmania.tubefy"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.ramzmania.tubefy"
        minSdk = 24
        targetSdk = 36
        versionCode = 6
        versionName = "5.03"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (keystoreProperties.getProperty("storeFile") != null) {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        // A separate "TubeFy Debug" app (own package, debug signature) that installs next to the release one
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            manifestPlaceholders["appLabel"] = "TubeFy Debug"
        }
        release {
            manifestPlaceholders["appLabel"] = "@string/app_name"
            signingConfigs.findByName("release")?.let { signingConfig = it }
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    // Two editions: "full" (GitHub releases, with usage analytics) and "foss" (F-Droid: only free
    // and open-source code, no Google libraries)
    flavorDimensions += "distribution"
    productFlavors {
        create("full") {
            dimension = "distribution"
        }
        create("foss") {
            dimension = "distribution"
        }
    }
    // F-Droid / IzzyOnDroid can't read the encrypted dependency list Android Studio adds to APKs
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
    compileOptions {
        // NewPipeExtractor relies on java.nio / java.time APIs that need desugaring below API 26
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }
    lint {
        disable += "MissingTranslation" + "TypographyFractions"
        abortOnError = false
        checkReleaseBuilds = false
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Usage analytics: "full" edition only
    "fullImplementation"(platform(libs.firebase.bom))
    "fullImplementation"(libs.firebase.analytics)
    coreLibraryDesugaring(libs.desugar.jdk.libs.nio)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.palette.ktx)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.runtime.livedata)
    debugImplementation(libs.androidx.ui.tooling)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Media3
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.ui)
    // Video page downloads: joins YouTube's separate HD video and audio into one MP4 (no re-encoding)
    implementation(libs.androidx.media3.transformer)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Networking / parsing
    implementation(libs.newpipeextractor)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.converter.scalars)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)
    implementation(libs.coil.compose)
    implementation(libs.reorderable)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
