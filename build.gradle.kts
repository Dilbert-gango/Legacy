plugins {
    id("com.android.application")
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.legacy_order"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.legacy_order"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {


    dependencies {
        // Stripe Payment
        implementation ("com.stripe:stripe-android:20.34.4")
        implementation ("com.squareup.okhttp3:okhttp:4.9.3")


            implementation ("androidx.viewpager2:viewpager2:1.0.0") // Or latest version
            implementation ("com.google.android.material:material:1.10.0") // Or latest version for TabLayout
        // Add this if you get androidx.annotation errors
        implementation ("androidx.annotation:annotation:1.3.0")
    }

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.stripe:stripe-android:20.34.4") // fixed here
    implementation("com.squareup.okhttp3:okhttp:4.9.3") // fixed here // //

}