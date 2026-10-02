plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.gdfelt.soundtile"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.gdfelt.soundtile"
        minSdk = 35
        targetSdk = 36
        versionCode = 4
        versionName = "2.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
