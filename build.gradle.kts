plugins {
    alias(libs.plugins.kotlinx.binary.compatibility.validator)
    alias(libs.plugins.nexus.publish)
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.junit5) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.wire) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
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

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
        }
    }
}
