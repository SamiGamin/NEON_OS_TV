import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.launcher.samiboxtv"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.launcher.samiboxtv"
        minSdk = 26
        targetSdk = 36
        versionCode = 16
        versionName = "1.1.0"

        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }

        val githubToken = properties.getProperty("GITHUB_TOKEN") ?: System.getenv("GITHUB_TOKEN") ?: ""
        val githubOwner = properties.getProperty("GITHUB_OWNER") ?: System.getenv("GITHUB_OWNER") ?: "SamiGamin"
        val githubRepo = properties.getProperty("GITHUB_REPO") ?: System.getenv("GITHUB_REPO") ?: "SamiBoxTV"

        buildConfigField("String", "GITHUB_TOKEN", "\"$githubToken\"")
        buildConfigField("String", "GITHUB_OWNER", "\"$githubOwner\"")
        buildConfigField("String", "GITHUB_REPO", "\"$githubRepo\"")
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_FILE")
                ?: (project.findProperty("KEYSTORE_FILE") as? String)
                ?: "release.keystore"
            val storePass = System.getenv("KEYSTORE_PASSWORD")
                ?: (project.findProperty("KEYSTORE_PASSWORD") as? String)
            val kAlias = System.getenv("KEY_ALIAS")
                ?: (project.findProperty("KEY_ALIAS") as? String)
            val kPass = System.getenv("KEY_PASSWORD")
                ?: (project.findProperty("KEY_PASSWORD") as? String)

            if (file(keystorePath).exists() && storePass != null && kAlias != null && kPass != null) {
                storeFile = file(keystorePath)
                storePassword = storePass
                keyAlias = kAlias
                keyPassword = kPass
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isCrunchPngs = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null) {
                signingConfig = releaseSigning
            } else {
                signingConfig = signingConfigs.getByName("debug")
            }
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

tasks.register("renameReleaseApk") {
    doLast {
        val releaseDir = layout.buildDirectory.dir("outputs/apk/release").get().asFile
        val vName = android.defaultConfig.versionName ?: "1.0"
        val apkFile = File(releaseDir, "app-release.apk")
        val unsignedFile = File(releaseDir, "app-release-unsigned.apk")
        val source = if (apkFile.exists()) apkFile else unsignedFile
        if (source.exists()) {
            val target = File(releaseDir, "NEONOS_TV-v$vName-release.apk")
            source.copyTo(target, overwrite = true)
            println("==> APK Release generado: ${target.name}")
        }
    }
}

tasks.register("renameDebugApk") {
    doLast {
        val debugDir = layout.buildDirectory.dir("outputs/apk/debug").get().asFile
        val vName = android.defaultConfig.versionName ?: "1.0"
        val apkFile = File(debugDir, "app-debug.apk")
        if (apkFile.exists()) {
            val target = File(debugDir, "NEONOS_TV-v$vName-debug.apk")
            apkFile.copyTo(target, overwrite = true)
            println("==> APK Debug generado: ${target.name}")
        }
    }
}

tasks.register("printVersionName") {
    doLast {
        println(android.defaultConfig.versionName ?: "1.0")
    }
}

afterEvaluate {
    tasks.findByName("assembleRelease")?.finalizedBy("renameReleaseApk")
    tasks.findByName("assembleDebug")?.finalizedBy("renameDebugApk")
}

base {
    archivesName.set("NEONOS_TV-v${android.defaultConfig.versionName}")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // 1. Jetpack Compose for TV (Crucial para el control remoto y grillas de TV)
    implementation(libs.androidx.tv.foundation.v100)
    implementation(libs.androidx.tv.material.v110)

    // 2. Coil (Para extraer y dibujar los iconos de las apps súper rápido)
    implementation(libs.coil.compose)

    // media3
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)

    implementation("androidx.activity:activity-compose:1.13.0")

    // Motor base y UI de Media3


    // Imprescindible para IPTV en vivo (HLS / m3u8)
    implementation(libs.androidx.media3.exoplayer.hls)

    // Soporte para conexiones de red HTTP/HTTPS robustas
    implementation(libs.androidx.media3.datasource.okhttp)

    // Generador de códigos QR
    implementation("com.google.zxing:core:3.5.3")
}