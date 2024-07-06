import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

buildscript {
    val kotlin_version = "1.9.10"

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.1.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
        classpath("org.jacoco:org.jacoco.core:0.8.7")
    }
}

plugins {
    id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false
}

allprojects {
    //apply("jacoco")

    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
    /*afterEvaluate {
        tasks.withType(JavaCompile.class) {
            options.compilerArgs << "-Xmaxerrs" << "1000"
        }
    }*/
}

subprojects {
    afterEvaluate {
        tasks.withType<KotlinJvmCompile> {
            kotlinOptions {
                jvmTarget = JavaVersion.VERSION_17.toString()
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
    compileSdkVersion(34)

    defaultConfig {
        minSdk = 21
        targetSdk = 34
        buildToolsVersion = "34.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    delete(rootProject.buildDir)
}

/*jacoco {
    toolVersion = "0.8.7"
}


tasks.register("jacocoFullReport", JacocoReport) {
    group = "Reporting"
    description = "Generate Jacoco coverage reports for the debug build"

    reports {
        html {
            enabled true
            destination file("build/reports/jacoco/html")
        }
        xml {
            enabled true
            destination file("build/reports/jacoco/jacocoFullReport.xml")
        }
    }

    //dependsOn ":app:testDebugUnitTest"
    dependsOn ":api:testDebugUnitTest"
    dependsOn ":db:testDebugUnitTest"
    //dependsOn ":app:connectedAndroidTest"
    dependsOn ":db:connectedAndroidTest"*/

//final fileFilter = ["**/R.class", "**/R\$*.class", "**/BuildConfig.*", "**/Manifest*.*", "android/**/*.*"]

/*    classDirectories.setFrom files([
            //fileTree(dir: "$project.rootDir/app/build/intermediates/javac/debug", excludes: fileFilter),
            //fileTree(dir: "$project.rootDir/app/build/tmp/kotlin-classes/debug", excludes: fileFilter),
            fileTree(dir: "$project.rootDir/api/build/intermediates/javac/debug", excludes: fileFilter),
            fileTree(dir: "$project.rootDir/api/build/tmp/kotlin-classes/debug", excludes: fileFilter),
            fileTree(dir: "$project.rootDir/db/build/tmp/kotlin-classes/debug", excludes: fileFilter),
    ])
    def coverageSourceDirs = [
            "$project.rootDir/app/src/main/java",
            "$project.rootDir/api/src/main/java",
            "$project.rootDir/db/src/main/java",
    ]

    additionalSourceDirs.setFrom files(coverageSourceDirs)
    sourceDirectories.setFrom files(coverageSourceDirs)
    executionData.setFrom fileTree(dir: project.rootDir, includes: [
            "app/jacoco.exec",
            "db/jacoco.exec",
            "api/jacoco.exec",
            "app/build/outputs/code_coverage/debugAndroidTest/connected/*-coverage.ec",
            "db/build/outputs/code_coverage/debugAndroidTest/connected/*-coverage.ec"
    ])
}*/
