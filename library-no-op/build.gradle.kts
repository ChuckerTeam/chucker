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

    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs +
            listOf(
                "-module-name",
                "com.github.ChuckerTeam.Chucker.library-no-op",
                "-Xexplicit-api=strict",
            )
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

dependencies {
    api(libs.okhttp)
    implementation(libs.jetbrains.kotlin.stdlib)
}

apply(from = rootProject.file("gradle/gradle-mvn-push.gradle"))
apply(from = rootProject.file("gradle/kotlin-static-analysis.gradle"))
