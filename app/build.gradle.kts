plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)

    // ksp
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.elfrikiamv.minegocio_puntodeventa"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.elfrikiamv.minegocio_puntodeventa"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        signingConfig = signingConfigs.getByName("debug")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }
}

// خروجی اسکیمای JSON Room برای مهاجرت‌های آینده (app/schemas)
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //splashscreen API
    implementation(libs.androidx.core.splashscreen)

    // Jetpack Compose
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.foundation)

    // اسکنر بارکد (ZXing-Android-Embedded — اسکن پیوسته با CaptureManager)
    implementation(libs.zxing.android.embedded)

    // Calendario Jalali (persa) para fechas شمسی
    implementation(libs.persiandate)

    // نمودارها (MPAndroidChart — line/bar/pie)
    implementation(libs.mpandroidchart)

    // خروجی اکسل گزارش‌ها (Apache POI + Aalto برای اندروید)
    implementation(libs.poi.ooxml)
    implementation(libs.aalto.xml)

    // نقشهٔ فروشگاه (osmdroid — جایگزین Leaflet در اندروید)
    implementation(libs.osmdroid)

    // کلاینت WebSocket (تب سرور در تنظیمات)
    implementation(libs.okhttp)

    // تولید QR برای ورود کاربران (هم‌نسخهٔ zxing-android-embedded)
    implementation(libs.zxing.core)

    // Room Database
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    //gson
    implementation(libs.gson)

    testImplementation("org.mockito:mockito-core:5.21.0")
}
