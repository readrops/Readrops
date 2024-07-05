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
        arg("room.schemaLocation", "$projectDir/schemas".toString())
        arg("room.incremental", "true")
    }
}



dependencies {
    coreLibraryDesugaring(libs.jdk.desugar)

    api("androidx.core:core-ktx:1.6.0")
    api("androidx.appcompat:appcompat:1.3.0")
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.0")

    testImplementation("junit:junit:4.13")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    implementation(libs.bundles.room)
    kapt(libs.room.compiler)
    androidTestImplementation(libs.room.testing)

    implementation(libs.bundles.paging)

    api("joda-time:joda-time:2.10.10") //TODO replace with java.time?

    implementation(libs.bundles.koin)
    testImplementation(libs.bundles.kointest)

    implementation(libs.bundles.coroutines)
    androidTestImplementation(libs.coroutines.test)
}
