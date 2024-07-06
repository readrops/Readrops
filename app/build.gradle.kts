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
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(project(":api"))
    implementation(project(":db"))

    coreLibraryDesugaring(libs.jdk.desugar)

    implementation(libs.corektx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.palette)
    implementation(libs.workmanager)
    implementation(libs.encrypted.preferences)

    implementation(libs.jsoup)
    implementation(libs.jodatime)

    testImplementation(libs.junit4)
    androidTestImplementation(libs.bundles.test)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)

    implementation(libs.bundles.voyager)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.coil)

    implementation(libs.bundles.coroutines)
    androidTestImplementation(libs.coroutines.test)

    implementation(libs.bundles.room)
    implementation(libs.bundles.paging)

    implementation(libs.bundles.koin)
    androidTestImplementation(libs.bundles.kointest)

    androidTestImplementation(libs.okhttp.mockserver)
}