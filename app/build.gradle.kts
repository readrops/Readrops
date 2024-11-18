plugins {
    id("com.android.application")
    kotlin("android")
    alias(libs.plugins.compose.compiler)
    id("com.mikepenz.aboutlibraries.plugin")
}


android {
    namespace = "com.readrops.app"

    defaultConfig {
        applicationId = "com.readrops.app"

        versionCode = 19
        versionName = "2.0.2"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        debug {
            isMinifyEnabled = false
            isShrinkResources = false

            applicationIdSuffix = ".debug"
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true

            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }

        create("beta") {
            initWith(getByName("release"))

            applicationIdSuffix = ".beta"
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
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
    implementation(libs.datastore)
    implementation(libs.browser)
    implementation(libs.splashscreen)
    implementation(libs.preferences)

    implementation(libs.jsoup)

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

    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)
    androidTestImplementation(libs.bundles.kointest)

    androidTestImplementation(libs.okhttp.mockserver)

    implementation(libs.aboutlibraries.composem3)
}