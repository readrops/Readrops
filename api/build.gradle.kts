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

    testImplementation("junit:junit:4.13")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    implementation(libs.bundles.koin)
    testImplementation(libs.bundles.kointest)

    implementation(libs.konsumexml)
    implementation(libs.kotlinxmlbuilder)

    implementation(libs.okhttp)
    testImplementation(libs.okhttp.mockserver)

    implementation("com.squareup.retrofit2:retrofit:2.9.0") {
        exclude("com.squareup.okhttp3", "okhttp3")
    }
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0") {
        exclude("com.squareup.moshi", "moshi")
    }

    implementation("com.squareup.moshi:moshi:1.15.1")

    api("org.jsoup:jsoup:1.13.1")

    debugApi("com.chimerapps.niddler:niddler:1.5.5")
    releaseApi("com.chimerapps.niddler:niddler-noop:1.5.5")

    testImplementation(libs.coroutines.test)
}
