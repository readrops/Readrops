plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.readrops.api"

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }

        create("beta") {
            initWith(getByName("release"))

            signingConfig = signingConfigs.getByName("debug")
        }
    }

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

        // disable lint rule which isn't supposed to be applied on a non compose module
        disable.add("CoroutineCreationDuringComposition")
    }
}

dependencies {
    implementation(project(":db"))

    coreLibraryDesugaring(libs.jdk.desugar)

    implementation(libs.coroutines.core)

    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)

    implementation(libs.konsumexml)
    implementation(libs.kotlinxmlbuilder)

    implementation(libs.okhttp)

    implementation(libs.bundles.retrofit) {
        exclude("com.squareup.okhttp3", "okhttp3")
        exclude("com.squareup.moshi", "moshi")
    }

    implementation(libs.moshi)
    implementation(libs.jsoup)

    testImplementation(libs.junit4)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.bundles.kointest)
    testImplementation(libs.okhttp.mockserver)
}
