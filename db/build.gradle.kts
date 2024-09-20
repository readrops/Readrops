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
}

dependencies {
    coreLibraryDesugaring(libs.jdk.desugar)

    implementation(libs.corektx)
    implementation(libs.appcompat)

    testImplementation(libs.junit4)
    androidTestImplementation(libs.bundles.test)

    implementation(libs.bundles.room)
    ksp(libs.room.compiler)
    androidTestImplementation(libs.room.testing)

    implementation(libs.bundles.paging)

    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)
    //androidTestImplementation(libs.bundles.kointest)
    // I don't know why but those dependencies are unreachable when accessed directly from version catalog
    androidTestImplementation("io.insert-koin:koin-test:${libs.versions.koin.bom.get()}")
    androidTestImplementation("io.insert-koin:koin-test-junit4:${libs.versions.koin.bom.get()}")

    implementation(libs.bundles.coroutines)
    androidTestImplementation(libs.coroutines.test)
}
