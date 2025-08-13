plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}
//val abiCodes = mapOf(
//    "armeabi-v7a" to 1,
//    "arm64-v8a" to 2,
//    "x86" to 3,
//    "x86_64" to 4
//)
val universalBase = 8000
android {
    namespace = "com.oneblue3.movieapp_teat"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.oneblue3.movieapp_teat"
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
        viewBinding = true
    }
//    splits {
//        abi {
//            isEnable = true
//            reset()
//            // include only the ABIs that were actually built
//            //noinspection WrongGradleMethod
//            abiCodes.keys.forEach { abi ->
////                D:\Projects\Android\OmniPlayer\library\src\androidMain
//                if (project.layout.projectDirectory.dir("src/androidMain/jniLibs/${abi}").asFile.exists()) {
//                    include(abi)
//                }
//            }
//            isUniversalApk = true // build an APK with all ABIs too
//        }
//    }
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.runtime.android)
    implementation(libs.foundation.layout.android)
    implementation(libs.media3.ui)
    implementation(libs.material3.android)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
//    api(project(":library")) // 引入 Omniplayer 核心库
    implementation(libs.media3.exoplayer) // ExoPlayer 依赖
    implementation(project(":library")) // 引入 Omniplayer 核心库

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3") // 检查下最新版本
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0") // lifecycleScope 需要

}