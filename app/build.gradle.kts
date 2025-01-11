import java.util.Properties

plugins {
    id("com.google.gms.google-services")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.1.0"
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

val placesApiKey: String = project.rootProject.file("local.properties")
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
            buildConfigField("String", "PLACES_API_KEY", "\"$placesApiKey\"")
        }
        release {
            buildConfigField("String", "PLACES_API_KEY", "\"$placesApiKey\"")
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

    secrets {
        propertiesFileName = "secrets.properties"
        defaultPropertiesFileName = "local.defaults.properties"
    }
}

dependencies {
    implementation(libs.ui)
    implementation(libs.material3)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.timber)
    implementation(libs.coil.compose)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.material3)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.places)
    implementation(libs.androidx.appcompat)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.play.services.auth)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.gson)
    implementation(libs.googleid)
    implementation(libs.androidx.activity.compose)
    implementation(libs.accompanist.navigation.material)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.maps.compose)
    implementation(libs.maps.ktx)
    implementation(libs.maps.utils.ktx)
    implementation(libs.androidx.runtime.livedata)
}