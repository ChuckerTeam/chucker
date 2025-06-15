plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.squareup.wire")
    id("com.apollographql.apollo3")
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
    implementation("androidx.test:runner:${rootProject.extra["androidXTestRunner"]}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${rootProject.extra["androidXExpresso"]}")
    androidTestImplementation("androidx.test.ext:junit:${rootProject.extra["androidXExtJUnit"]}")
    debugImplementation(project(":library"))
    releaseImplementation(project(":library-no-op"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib:${rootProject.extra["kotlinVersion"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${rootProject.extra["coroutineVersion"]}")
    implementation("androidx.activity:activity-ktx:${rootProject.extra["activityVersion"]}")

    implementation("com.google.android.material:material:${rootProject.extra["materialComponentsVersion"]}")
    implementation("androidx.appcompat:appcompat:${rootProject.extra["appCompatVersion"]}")
    implementation("androidx.constraintlayout:constraintlayout:${rootProject.extra["constraintLayoutVersion"]}")

    implementation("com.squareup.okhttp3:logging-interceptor:${rootProject.extra["okhttpVersion"]}")
    implementation("com.squareup.retrofit2:retrofit:${rootProject.extra["retrofitVersion"]}")
    implementation("com.squareup.retrofit2:converter-gson:${rootProject.extra["retrofitVersion"]}")

    implementation("com.apollographql.apollo3:apollo-runtime:${rootProject.extra["apolloVersion"]}")

    debugImplementation("com.squareup.leakcanary:leakcanary-android:${rootProject.extra["leakcanaryVersion"]}")
}

apply(from = rootProject.file("gradle/kotlin-static-analysis.gradle"))
