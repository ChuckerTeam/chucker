import java.io.File

plugins {
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.17.0"
    id("io.codearte.nexus-staging") version "0.30.0"
}

buildscript {
    val kotlinVersion by extra("2.1.21")
    val androidGradleVersion by extra("8.10.1")
    val coroutineVersion by extra("1.10.2")

    // Google libraries
    val activityVersion by extra("1.10.1")
    val appCompatVersion by extra("1.7.1")
    val constraintLayoutVersion by extra("2.2.1")
    val materialComponentsVersion by extra("1.12.0")
    val fragmentVersion by extra("1.8.8")
    val roomVersion by extra("2.7.2")
    val lifecycleVersion by extra("2.9.1")
    val androidXCoreVersion by extra("2.2.0")
    val paletteKtxVersion by extra("1.0.0")
    val kspVersion by extra("2.1.21-2.0.2")

    // Networking
    val brotliVersion by extra("0.1.2")
    val gsonVersion by extra("2.13.1")
    val okhttpVersion by extra("4.12.0")
    val retrofitVersion by extra("3.0.0")
    val wireVersion by extra("5.3.3")

    // Debug and quality control
    val binaryCompatibilityValidator by extra("0.17.0")
    val detektVersion by extra("1.23.8")
    val ktLintGradleVersion by extra("12.3.0")
    val leakcanaryVersion by extra("2.14")

    // Apollo
    val apolloVersion by extra("3.8.6")

    // Testing
    val androidxTestCoreVersion by extra("1.6.1")
    val junitGradlePluginVersion by extra("1.13.0.0")
    val junitVersion by extra("5.13.1")
    val junitPlatformLauncherVersion by extra("1.13.1")
    val junit4Version by extra("4.13.2")
    val mockkVersion by extra("1.14.2")
    val robolectricVersion by extra("4.14.1")
    val truthVersion by extra("1.4.4")
    val androidXTestRunner by extra("1.6.2")
    val androidXTestRules by extra("1.6.1")
    val androidXTestExt by extra("1.2.1")
    val androidXExpresso by extra("3.6.1")
    val androidXExtJUnit by extra("1.2.1")

    // Publishing
    val nexusStagingPlugin by extra("0.30.0")

    repositories {
        google()
        gradlePluginPortal()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:$androidGradleVersion")
        classpath("de.mannodermaus.gradle.plugins:android-junit5:$junitGradlePluginVersion")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.apollographql.apollo3:apollo-gradle-plugin:$apolloVersion")
        classpath("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:$detektVersion")
        classpath("org.jlleitschuh.gradle:ktlint-gradle:$ktLintGradleVersion")
        classpath("org.jetbrains.kotlinx:binary-compatibility-validator:$binaryCompatibilityValidator")
        classpath("com.squareup.wire:wire-gradle-plugin:$wireVersion")
        classpath("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:$nexusStagingPlugin")
        classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:$kspVersion")
    }
}

apiValidation {
    ignoredProjects.add("sample")
    ignoredPackages.addAll(
        listOf(
            "com.chuckerteam.chucker.internal",
            "com.chuckerteam.chucker.databinding",
        ),
    )
}

allprojects {
    val VERSION_NAME: String by project
    val GROUP: String by project

    version = VERSION_NAME
    group = GROUP

    repositories {
        google()
        mavenCentral()
    }

    tasks.withType<Test> {
        testLogging {
            events("skipped", "failed", "passed")
        }
    }
}

tasks.register<Copy>("installGitHook") {
    from(File(rootProject.rootDir, "pre-commit"))
    into(File(rootProject.rootDir, ".git/hooks"))

    doLast {
        val hookFile = File(rootProject.rootDir, ".git/hooks/pre-commit")
        if (hookFile.exists()) {
            hookFile.setExecutable(true)
        }
    }
}

tasks.register<Delete>("clean") {
    dependsOn("installGitHook")
    delete(rootProject.buildDir)
}

extra.apply {
    set("minSdkVersion", 21)
    set("targetSdkVersion", 35)
    set("compileSdkVersion", 35)
}

configure<io.codearte.gradle.nexus.NexusStagingExtension> {
    username = findProperty("NEXUS_USERNAME") as String?
    password = findProperty("NEXUS_PASSWORD") as String?
    stagingProfileId = "ea09119de9f4"
}
