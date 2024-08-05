plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.readrops.api"

    sourceSets {
        getByName("androidTest") {
            assets.srcDirs("$projectDir/androidTest/assets")
        }
    }

    kotlinOptions {
        freeCompilerArgs = listOf("-Xstring-concat=inline")
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(project(":db"))

    coreLibraryDesugaring(libs.jdk.desugar)

    testImplementation(libs.junit4)

    implementation(libs.coroutines.core)
    testImplementation(libs.coroutines.test)

    implementation(libs.bundles.koin)
    testImplementation(libs.bundles.kointest)

    implementation(libs.konsumexml)
    implementation(libs.kotlinxmlbuilder)

    implementation(libs.okhttp)
    testImplementation(libs.okhttp.mockserver)

    implementation(libs.bundles.retrofit) {
        exclude("com.squareup.okhttp3", "okhttp3")
        exclude("com.squareup.moshi", "moshi")
    }

    implementation(libs.moshi)
    implementation(libs.jsoup)
}
