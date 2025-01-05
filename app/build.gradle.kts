import java.util.Properties

plugins {
    id("com.google.gms.google-services")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.1.0"
}

val apiKey: String = project.rootProject.file("local.properties")
    .takeIf { it.exists() }
    ?.let { Properties().apply { load(it.inputStream()) } }
    ?.getProperty("PLACES_API_KEY") ?: ""

android {
    namespace = "com.denisshulika.road_radar"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.denisshulika.road_radar"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "PLACES_API_KEY", "\"$apiKey\"")
        }
        release {
            buildConfigField("String", "PLACES_API_KEY", "\"$apiKey\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.ui)
    implementation(libs.material3)
    implementation(libs.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.play.services.auth)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.googleid)
    implementation(libs.androidx.espresso.core)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.animation.core.android)
    implementation(libs.places)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.coil)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)
    implementation(libs.hilt.android)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.mockito.core)
    implementation(libs.accompanist.navigation.material)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.timber)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.ui.text)
    implementation(libs.libphonenumber)
    implementation (libs.coil.compose)
    implementation (libs.androidx.datastore.preferences)
    implementation (libs.androidx.datastore)
    implementation (libs.play.services.location)
}