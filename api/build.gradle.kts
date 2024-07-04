plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.readrops.api"
    compileSdkVersion(34)

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(34)
        buildToolsVersion("34.0.0")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("androidTest") {
            assets.srcDirs("$projectDir/androidTest/assets")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }

        debug {
            isMinifyEnabled = false
            isTestCoverageEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xstring-concat=inline")
    }
    lint {
        abortOnError = false
    }
}

dependencies {
    //implementation fileTree(dir: "libs", include: ["*.jar"])
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
