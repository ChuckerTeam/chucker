plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.wire)
    alias(libs.plugins.apollo)
    alias(libs.plugins.compose.compiler)
}

wire {
    kotlin {}
}

android {
    compileSdk = rootProject.extra["compileSdkVersion"] as Int
    namespace = "com.chuckerteam.chucker.sample"

    defaultConfig {
        minSdk = rootProject.extra["minSdkVersion"] as Int
        targetSdk = rootProject.extra["targetSdkVersion"] as Int
        applicationId = "com.chuckerteam.chucker.sample"
        versionName = rootProject.extra["VERSION_NAME"] as String
        versionCode = (rootProject.extra["VERSION_CODE"] as String).toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = false
        compose = true
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    signingConfigs {
        getByName("debug") {
            keyAlias = "chucker"
            keyPassword = "android"
            storeFile = file("debug.keystore")
            storePassword = "android"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        jvmToolchain(11)
    }

    lint {
        abortOnError = true
        disable.addAll(listOf("AcceptsUserCertificates", "GradleDependency"))
        warningsAsErrors = true
    }
}

apollo {
    service("rickandmortyapi") {
        packageName.set("com.chuckerteam.chucker.sample")
        schemaFile.set(file("src/main/graphql/com/chuckerteam/chucker/sample/schema.json.graphql"))
        srcDir("src/main/graphql")
        excludes.add("**/schema.json.graphql")
        excludes.add("**/schema.json")
    }
}

dependencies {
    implementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    implementation(project(":library"))
    releaseImplementation(project(":library-no-op"))

    implementation(libs.jetbrains.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.activity.ktx)

    implementation(libs.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.logging.interceptor)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    implementation(libs.apollo.runtime)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.material3.window.size)

    debugImplementation(libs.leakcanary.android)
}

apply(from = rootProject.file("gradle/kotlin-static-analysis.gradle"))
