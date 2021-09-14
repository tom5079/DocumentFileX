plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdk = 30
    buildToolsVersion = "30.0.3"

    defaultConfig {
        applicationId = "xyz.quaver.io.sample"
        minSdk = 21
        targetSdk = 30
        versionCode = 1
        versionName = project.version.toString()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.0.2"
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("androidx.core:core-ktx:1.6.0")

    implementation("androidx.activity:activity-compose:1.3.1")

    implementation("androidx.compose.ui:ui:1.0.2")
    implementation("androidx.compose.ui:ui-tooling:1.0.2")
    implementation("androidx.compose.foundation:foundation:1.0.2")
    implementation("androidx.compose.material:material:1.0.2")
    implementation("androidx.compose.material:material-icons-core:1.0.2")
    implementation("androidx.compose.material:material-icons-extended:1.0.2")
    implementation("androidx.compose.animation:animation:1.0.2")
    implementation("androidx.compose.runtime:runtime:1.0.2")
    implementation("androidx.compose.runtime:runtime-livedata:1.0.2")
    implementation("com.google.accompanist:accompanist-appcompat-theme:0.16.0")

    implementation(project(":library"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

}
