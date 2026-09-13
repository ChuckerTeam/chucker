import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    compileSdk = rootProject.extra["compileSdkVersion"] as Int
    namespace = "com.chuckerteam.chucker"

    // Build with the JDK 21 toolchain but emit Java 17 bytecode so the
    // published library stays consumable by JDK 17 projects (see #1660).
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(21)
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
        jvmTarget = JvmTarget.JVM_17
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
