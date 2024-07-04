plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.parcelize")
}

android {
    namespace = "com.readrops.db"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        targetSdk = 34
        buildToolsVersion ="34.0.0"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.incremental" to "true",
                    "room.schemaLocation" to "$projectDir/schemas".toString()
                )
            }
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    sourceSets {
        getByName("androidTest") {
            assets.srcDirs("$projectDir/schemas")
        }
    }


    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
