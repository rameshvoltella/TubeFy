plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
//    id("com.google.devtools.ksp")
//    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    kotlin("kapt")

}

android {
    namespace = "com.ramzmania.tubefy"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ramzmania.tubefy"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    //testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.github.TeamNewPipe:NewPipeExtractor:0.24.2")
    implementation ("com.github.kotvertolet:youtube-jextractor:v0.3.4")
    implementation("com.squareup.okhttp3:okhttp:2.3.0")
    implementation ("com.google.code.gson:gson:2.8.6")
    implementation  ("org.mozilla:rhino:1.7.13")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation("androidx.navigation:navigation-runtime-ktx:2.7.7")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.15.1")
   implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    // Hiltsa
  /*  implementation("com.google.dagger:hilt-android:2.50")
    implementation("androidx.navigation:navigation-runtime-ktx:2.7.7")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-common:1.2.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("com.google.dagger:hilt-android-compiler:2.50")*/
}
kapt {
    correctErrorTypes = true
}