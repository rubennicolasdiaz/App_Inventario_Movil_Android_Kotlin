plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.serialization)
}



android {
    namespace = "com.example.indotinventario"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.indotinventario"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // Para activar el uso de View Binding:
    buildFeatures{
        viewBinding = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }


}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    //Splash API
    implementation(libs.coreSplashscreen)

    //Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.squareup.okhttp)
    implementation(libs.squareup.logging.interceptor)

    //MotionToast
    implementation(libs.motionToast)


    implementation(libs.mssqlJdbc)
    implementation(libs.coreSplashscreen )

    implementation(libs.zxingAndroidEmbedded)
    implementation(libs.gson)

    // Corrutinas:
    implementation(libs.activityKtx)
    implementation(libs.lifecycleViewModelKtx)

    implementation(libs.zxing) //Lector código barras
    implementation(libs.filament.android)
    //implementation(libs.firebase.firestore.ktx) // Implementar en caso de Firebase

    implementation(files("libs/jtds-1.3.1.jar"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //Supabase:
    implementation(libs.supabase)
    implementation(libs.ktorclient)

    //Serialización Kotlinx:
    implementation(libs.serialization.json)

}


