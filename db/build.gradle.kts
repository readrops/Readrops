plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.parcelize")
}

android {
    namespace = "com.readrops.db"

    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.incremental" to "true",
                    "room.schemaLocation" to "$projectDir/schemas".toString()
                )
            }
        }

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

// Needed for kapt starting with kotlin plugin 1.5
kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
    }
}

dependencies {
    coreLibraryDesugaring(libs.jdk.desugar)

    implementation(libs.corektx)
    implementation(libs.appcompat)

    testImplementation(libs.junit4)
    androidTestImplementation(libs.bundles.test)

    implementation(libs.bundles.room)
    kapt(libs.room.compiler)
    androidTestImplementation(libs.room.testing)

    implementation(libs.bundles.paging)

    implementation(libs.jodatime)

    implementation(libs.bundles.koin)
    testImplementation(libs.bundles.kointest)

    implementation(libs.bundles.coroutines)
    androidTestImplementation(libs.coroutines.test)
}
