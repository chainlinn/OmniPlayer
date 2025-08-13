import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "io.github.zhangmanyue774"
version = "beta-1.0.0"

//val abiCodes = mapOf(
//    "armeabi-v7a" to 1,
//    "arm64-v8a" to 2,
//    "x86" to 3,
//    "x86_64" to 4
//)
//val universalBase = 8000

kotlin {
    jvm()
    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("androidx.media3:media3-exoplayer:1.7.1")
                //jvm注解
                implementation("androidx.annotation:annotation-jvm:1.9.1")

            }
        }
    }
}

android {
    namespace = "io.github.zhangmanyue774.omniplayer.kernel"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()

//        ndk {
//            //armeabi-v7a": 1, "arm64-v8a": 2, "x86":3, "x86_64":4
//            // Specifies the ABI configurations of your native
//            // libraries Gradle should build and package with your app.
//            abiFilters += listOf("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
//        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    sourceSets {
        getByName("main") {
            // 1. 强制指定 JNI 库的来源
            //    告诉 AGP，只从这个目录找 .so 文件
//            jniLibs.srcDirs("src/androidMain/kotlin/jniLibs")
            // 2. 【新增的关键配置】强制指定 Kotlin/Java 源码的来源
            //    告诉 AGP，除了默认的 kotlin 目录，也必须包含这个目录
            //    这可以解决一些罕见的插件冲突或缓存问题

//            配置最为关键的代码目录，告诉AGP编译该目录下的so
            java.srcDirs("src/androidMain/kotlin")
            // 如果你的 MPVLib 是 .java 文件，就用下面这行
            // java.srcDirs("src/androidMain/java")
        }
    }
}


mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates(group.toString(), "library", version.toString())

    pom {
        name = "OmniPlayer Kernel Library"
        //兼容多平台的视频播放器内核
        description = "A multiplatform video player kernel library compatible with multiple platforms."
        inceptionYear = "2025"
        url = "https://github.com/zhangmanyue774/OmniPlayer"
        licenses {
            license {
                // 使用标准的 SPDX 许可证名称和链接
                name = "GNU Affero General Public License v3.0"
                url = "https://www.gnu.org/licenses/agpl-3.0.html"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "zhangmanyue774"
                name = "santu"
                url = "https://github.com/zhangmanyue774"
            }
        }
        scm {
            url = "https://github.com/zhangmanyue774/OmniPlayer.git"
            connection = "scm:git://github.com/zhangmanyue774/OmniPlayer.git"
            developerConnection = "scm:git://github.com/zhangmanyue774/OmniPlayer.git"
        }
    }
}
