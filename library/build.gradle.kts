import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
}

android {
    compileSdk = rootProject.extra["compileSdkVersion"] as Int
    namespace = "com.chuckerteam.chucker"

    defaultConfig {
        minSdk = rootProject.extra["minSdkVersion"] as Int
        consumerProguardFiles("proguard-rules.pro")
        resValue("string", "chucker_version", rootProject.extra["VERSION_NAME"] as String)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        viewBinding = true
        buildConfig = false
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.useJUnitPlatform()
            }
        }
    }

    resourcePrefix = "chucker_"

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }

    lint {
        abortOnError = true
        disable.addAll(listOf("RtlEnabled", "GradleDependency"))
        warningsAsErrors = true
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-module-name",
            "com.github.ChuckerTeam.Chucker.library",
            "-Xexplicit-api=strict",
        )
    }
}

dependencies {
    implementation(libs.jetbrains.kotlin.stdlib)

    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.palette.ktx)

    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.gson)
    implementation(libs.dec)

    api(libs.okhttp)
    api(libs.okhttp3.okhttp)
    testImplementation(libs.mockwebserver)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testRuntimeOnly(libs.junit.vintage.engine)

    testImplementation(libs.junit)
    testImplementation(libs.junit4)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.core)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.truth)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.androidx.junit)
}

apply(from = rootProject.file("gradle/gradle-mvn-push.gradle"))
apply(from = rootProject.file("gradle/kotlin-static-analysis.gradle"))
