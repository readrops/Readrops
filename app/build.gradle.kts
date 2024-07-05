plugins {
    id("com.android.application")
    kotlin("android")
}


android {
    namespace = "com.readrops.app"

    defaultConfig {
        applicationId = "com.readrops.app"

        versionCode = 15
        versionName = "2.0-beta01"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        debug {
            isMinifyEnabled = false
            isShrinkResources = false

            isTestCoverageEnabled = true
            applicationIdSuffix = ".debug"
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.0"
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(project(":api"))
    implementation(project(":db"))

    coreLibraryDesugaring(libs.jdk.desugar)

    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation(libs.bundles.compose)
    implementation(libs.compose.activity)

    implementation("androidx.palette:palette-ktx:1.0.0")

    implementation(libs.bundles.voyager)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.coil)

    implementation(libs.bundles.coroutines)
    androidTestImplementation(libs.coroutines.test)

    implementation(libs.bundles.room)
    implementation(libs.bundles.paging)

    implementation(libs.bundles.koin)
    androidTestImplementation(libs.bundles.kointest)

    androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.9.0")


    implementation(libs.encrypted.preferences)
    implementation(libs.work.manager)
}