plugins {
    id("com.android.application")
    id("checkstyle")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
    kotlin("android")
    kotlin("android.extensions")
}

android {
    compileSdkVersion(29)

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 169
        versionName = "0.7.16"
    }

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_1_8
        sourceCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    flavorDimensions("implementation")

    productFlavors {
        create("ui") {
            setDimension("implementation")
            matchingFallbacks = listOf("uiRelease")
        }
        create("skeleton") {
            setDimension("implementation")
            matchingFallbacks = listOf("skeletonRelease")
        }
    }
}

dependencies {
    // https://maven.google.com/web/index.html
    // https://developer.android.com/jetpack/androidx/releases/core
    val preferenceVersion = "1.1.1"
    val coreVersion = "1.2.0"
    val materialVersion = "1.1.0"
    val okDownload = "1.0.3"
    val activityVersion = "1.2.0-alpha04"
    val fragmentVersion = "1.3.0-alpha04"
    val roomVersion = "2.2.5"
    val coroutines = "1.3.4"
    val dagger = "2.27"

    implementation("androidx.annotation:annotation:1.1.0")
    implementation("androidx.core:core:$coreVersion")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.70")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.0.0")
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.squareup.okhttp3:okhttp:3.2.0")
    implementation("androidx.core:core:$coreVersion")
    implementation("androidx.core:core-ktx:$coreVersion")
    implementation("org.jetbrains.anko:anko-commons:0.10.4")
    implementation("androidx.fragment:fragment-ktx:$fragmentVersion")
    implementation("androidx.preference:preference:$preferenceVersion")
    implementation("androidx.preference:preference-ktx:$preferenceVersion")
    implementation("com.google.android.material:material:$materialVersion")
    implementation("androidx.webkit:webkit:1.2.0")

    implementation("androidx.activity:activity-ktx:$activityVersion")
    implementation("androidx.fragment:fragment-ktx:$fragmentVersion")
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("com.google.dagger:dagger:$dagger")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines")

    implementation("androidx.navigation:navigation-fragment-ktx:2.3.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.0")
    implementation("androidx.core:core-ktx:1.3.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")


    implementation("com.liulishuo.okdownload:okdownload:${okDownload}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.7")
    implementation("com.google.code.gson:gson:2.8.6")

    dependencies.add("uiImplementation", project(":openvpn"))
    dependencies.add("skeletonImplementation", project(":openvpn"))

    testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.72")
    testImplementation("junit:junit:4.13")
    testImplementation("org.mockito:mockito-core:3.3.3")
    testImplementation("org.robolectric:robolectric:4.3.1")
    testImplementation("androidx.test:core:1.2.0")


    kapt("androidx.room:room-compiler:$roomVersion")
    kapt("com.google.dagger:dagger-compiler:$dagger")

}
