plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
}

android {
    compileSdk = rootProject.extra["compileSdkVersion"] as Int
    namespace = "com.chuckerteam.chucker"

    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs +
            listOf(
                "-module-name",
                "com.github.ChuckerTeam.Chucker.library",
                "-Xexplicit-api=strict",
            )
    }

    defaultConfig {
        minSdk = rootProject.extra["minSdkVersion"] as Int
        consumerProguardFiles("proguard-rules.pro")
        resValue("string", "chucker_version", rootProject.extra["VERSION_NAME"] as String)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        jvmToolchain(11)
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

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${rootProject.extra["kotlinVersion"]}")

    implementation("com.google.android.material:material:${rootProject.extra["materialComponentsVersion"]}")
    implementation("androidx.constraintlayout:constraintlayout:${rootProject.extra["constraintLayoutVersion"]}")
    implementation("androidx.palette:palette-ktx:${rootProject.extra["paletteKtxVersion"]}")

    implementation("androidx.activity:activity-ktx:${rootProject.extra["activityVersion"]}")
    implementation("androidx.fragment:fragment-ktx:${rootProject.extra["fragmentVersion"]}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${rootProject.extra["lifecycleVersion"]}")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${rootProject.extra["lifecycleVersion"]}")
    implementation("androidx.room:room-ktx:${rootProject.extra["roomVersion"]}")
    implementation("androidx.room:room-runtime:${rootProject.extra["roomVersion"]}")
    ksp("androidx.room:room-compiler:${rootProject.extra["roomVersion"]}")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${rootProject.extra["coroutineVersion"]}")

    implementation("com.google.code.gson:gson:${rootProject.extra["gsonVersion"]}")

    implementation("org.brotli:dec:${rootProject.extra["brotliVersion"]}")

    api(platform("com.squareup.okhttp3:okhttp-bom:${rootProject.extra["okhttpVersion"]}"))
    api("com.squareup.okhttp3:okhttp")
    testImplementation("com.squareup.okhttp3:mockwebserver")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${rootProject.extra["junitVersion"]}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${rootProject.extra["junitVersion"]}")
    testImplementation("junit:junit:${rootProject.extra["junit4Version"]}")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:${rootProject.extra["junitVersion"]}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${rootProject.extra["junitVersion"]}")
    testImplementation("io.mockk:mockk:${rootProject.extra["mockkVersion"]}")
    testImplementation("androidx.test:core:${rootProject.extra["androidxTestCoreVersion"]}")
    testImplementation("androidx.arch.core:core-testing:${rootProject.extra["androidXCoreVersion"]}")
    testImplementation("com.google.truth:truth:${rootProject.extra["truthVersion"]}")
    testImplementation("org.robolectric:robolectric:${rootProject.extra["robolectricVersion"]}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${rootProject.extra["coroutineVersion"]}")

    androidTestImplementation("junit:junit:${rootProject.extra["junit4Version"]}")
    androidTestImplementation("androidx.test:runner:${rootProject.extra["androidXTestRunner"]}")
    androidTestImplementation("androidx.test:rules:${rootProject.extra["androidXTestRules"]}")
    androidTestImplementation("com.google.truth:truth:${rootProject.extra["truthVersion"]}")
    androidTestImplementation("androidx.test.ext:junit:${rootProject.extra["androidXTestExt"]}")
}

apply(from = rootProject.file("gradle/gradle-mvn-push.gradle"))
apply(from = rootProject.file("gradle/kotlin-static-analysis.gradle"))
