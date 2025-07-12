import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    compileSdk = rootProject.extra["compileSdkVersion"] as Int
    namespace = "com.chuckerteam.chucker"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        jvmToolchain(11)
    }

    buildFeatures {
        buildConfig = false
    }

    defaultConfig {
        minSdk = rootProject.extra["minSdkVersion"] as Int
    }

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
    api(libs.okhttp)
    implementation(libs.jetbrains.kotlin.stdlib)
}

apply(from = rootProject.file("gradle/gradle-mvn-push.gradle"))
apply(from = rootProject.file("gradle/kotlin-static-analysis.gradle"))
