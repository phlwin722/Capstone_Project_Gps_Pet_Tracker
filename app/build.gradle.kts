plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.gps_pet_tracker"
    compileSdk = 34

    packagingOptions {
        exclude("META-INF/javamail.providers")
        exclude("META-INF/mailcap")
    }

    defaultConfig {
        applicationId = "com.example.gps_pet_tracker"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        getByName("release") {
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
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.firebase:firebase-storage:21.0.0")
    implementation("com.google.firebase:firebase-firestore:25.1.0")
    implementation("com.github.dhaval2404:imagepicker:2.1")
    implementation("com.squareup.picasso:picasso:2.71828")

    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")

    // Also add the dependency for the Google Play services library and specify its version
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Implement Lottie animation library
    implementation("com.airbnb.android:lottie:3.4.0")

    // Add Firebase Firestore and Activity libraries
    implementation("com.google.firebase:firebase-firestore:25.0.0")
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.work:work-runtime:2.9.1")

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    // JavaMail dependencies
    implementation("com.sun.mail:android-mail:1.6.5")
    implementation("com.sun.mail:android-activation:1.6.5")
    implementation ("com.google.android.gms:play-services-measurement-api:22.0.2")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    // Other dependencies...

    implementation("com.mapbox.maps:android:10.18.3")
    implementation("com.mapbox.navigation:android:2.17.1") // Adjust version as necessary
    implementation("com.mapbox.navigation:core:2.15.0") // Adjust version as necessary
    implementation("com.mapbox.search:mapbox-search-android-ui:1.0.0-rc.6")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")// Retrofit for network requests
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0") // GSON converter for JSON parsing
    implementation(kotlin("script-runtime"))

}
