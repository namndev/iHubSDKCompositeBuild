plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.ihub.eidscan"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ihub.eidscan"
        minSdk = 26
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding=true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // MLKit -- AGAIN
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")
    //
    implementation("com.mobsandgeeks:android-saripaar:2.0.3")
    implementation("org.jmrtd:jmrtd:0.7.40")
    implementation("io.fotoapparat:fotoapparat:2.7.0")
    implementation("net.sf.scuba:scuba-sc-android:0.0.23")
    implementation("com.madgag.spongycastle:prov:1.58.0.0")
    implementation ("org.bouncycastle:bcpkix-jdk15on:1.70") {
        exclude(group="org.bouncycastle",module = "bcprov-jdk15on")
        exclude(group="org.bouncycastle",module = "bcutil-jdk15on")
    }
    //
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    //
    implementation("com.ihub.eidsdk:eidscan-remote:1.0.0")
}