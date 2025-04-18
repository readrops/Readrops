plugins {
    id("com.android.library")
    kotlin("android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.readrops.db"

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }

        create("beta") {
            initWith(getByName("release"))

            signingConfig = signingConfigs.getByName("debug")
        }
    }

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("androidTest") {
            assets.srcDirs("$projectDir/schemas")
        }
    }

    lint {
        abortOnError = false
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.generateKotlin", "true")
}

dependencies {
    coreLibraryDesugaring(libs.jdk.desugar)

    implementation(libs.corektx)
    implementation(libs.appcompat)

    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.paging)

    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)

    testImplementation(libs.junit4)

    androidTestImplementation(libs.bundles.test)
    androidTestImplementation(libs.bundles.kointest)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.coroutines.test)
}
