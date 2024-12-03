plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
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


    // Motion Toast: En el otro fichero build.gradle incluir: allprojects {
    //    repositories {
    //        google()
    //        mavenCentral()
    //        maven { url = uri("https://jitpack.io") }
    //    }
    //}
    implementation("com.github.Spikeysanju:MotionToast:1.3.3.4")




    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.code.gson:gson:2.8.6")
    // Corrutinas:
    implementation("androidx.activity:activity-ktx:1.7.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")

    // Navegación para bottom bar:
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.4")

    // Solo añade estas líneas si tienes los JAR correctos en libs
    implementation(files("libs/jtds-1.3.1.jar"))


    implementation("me.dm7.barcodescanner:zxing:1.9.8")
    implementation(libs.filament.android)
    implementation(libs.firebase.firestore.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}


