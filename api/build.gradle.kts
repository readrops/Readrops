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

    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)
    //testImplementation(libs.bundles.kointest)
    // I don't know why but those dependencies are unreachable when accessed directly from version catalog
    testImplementation("io.insert-koin:koin-test:${libs.versions.koin.bom.get()}")
   testImplementation("io.insert-koin:koin-test-junit4:${libs.versions.koin.bom.get()}")

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
