plugins {
    id("com.android.application")
}

android {
    namespace = "com.vexiquest.mapprototype"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.vexiquest.mapprototype"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0-northern-gateways-prototype"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
