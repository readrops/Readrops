import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.android.agp)
        classpath(libs.kotlin.kgp)
        classpath(libs.jacoco)
        classpath(libs.aboutlibraries)
    }
}

plugins {
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
    jacoco
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

subprojects {
    afterEvaluate {
        with(extensions.getByType<KotlinAndroidProjectExtension>()) {
            compilerOptions {
                jvmTarget = JvmTarget.JVM_17
            }
        }

        plugins.withType<AppPlugin> {
            configure<BaseExtension> {
                configure(this)
            }
        }
        plugins.withType<LibraryPlugin> {
            configure<LibraryExtension> {
                configure(this)
            }
        }
    }

}

fun configure(extension: BaseExtension) = with(extension) {
    compileSdkVersion(35)

    defaultConfig {
        minSdk = 21
        targetSdk = 35
        buildToolsVersion = "35.0.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        isCoreLibraryDesugaringEnabled = true
    }

    lintOptions.isAbortOnError = false

    /*dependencies {
        add("coreLibraryDesugaring", libs.jdk.desugar)
    }*/
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

jacoco {
    toolVersion = "0.8.7"
}


tasks.register<JacocoReport>("jacocoFullReport") {
    group = "Reporting"
    description = "Generate Jacoco coverage reports for the debug build"

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    dependsOn(":app:testDebugUnitTest")
    dependsOn(":app:connectedAndroidTest")
    dependsOn(":api:testDebugUnitTest")
    dependsOn(":db:testDebugUnitTest")
    dependsOn(":db:connectedAndroidTest")

    val excludeFilter = listOf(
        "**/R.class",
        "**/R\$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "android/**/*.*"
    )

    classDirectories.setFrom(
        files(
            fileTree("${project.rootDir}/app/build/intermediates/javac/debug") { exclude(excludeFilter) },
            fileTree("${project.rootDir}/app/build/tmp/kotlin-classes/debug") { exclude(excludeFilter) }
        ),
        fileTree("${project.rootDir}/api/build/tmp/kotlin-classes/debug")  { exclude(excludeFilter) },
        fileTree("${project.rootDir}/db/build/tmp/kotlin-classes/debug")  { exclude(excludeFilter) },
    )

    val coverageSourceDirs = listOf(
        "${project.rootDir}/app/src/main/java",
        "${project.rootDir}/api/src/main/java",
        "${project.rootDir}/db/src/main/java",
    )

    additionalSourceDirs.setFrom(files(coverageSourceDirs))
    sourceDirectories.setFrom(files(coverageSourceDirs))

    executionData.setFrom(
        fileTree(project.rootDir) {
            include(
                listOf(
                    "api/build/outputs/unit_test_code_coverage/**/*.exec",
                    "db/build/outputs/unit_test_code_coverage/**/*.exec",
                    "app/build/outputs/code_coverage/debugAndroidTest/connected/**/*.ec",
                    "db/build/outputs/code_coverage/debugAndroidTest/connected/**/*.ec"
                )
            )
        }
    )
}
