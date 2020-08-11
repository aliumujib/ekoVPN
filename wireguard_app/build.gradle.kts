plugins {
    id("com.android.library")
    id("checkstyle")
}

android {
    compileSdkVersion(29)

    defaultConfig {
        minSdkVersion(14)
        targetSdkVersion(29)
    }

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_1_8
        sourceCompatibility = JavaVersion.VERSION_1_8
    }

}


dependencies {

    val agpVersion = "4.0.0"
    val annotationsVersion = "1.1.0"
    val appcompatVersion = "1.1.0"
    val bintrayPluginVersion = "1.8.4"
    val biometricVersion = "1.0.1"
    val collectionVersion = "1.1.0"
    val constraintLayoutVersion = "1.1.3"
    val coordinatorLayoutVersion = "1.1.0"
    val coreKtxVersion = "1.3.0"
    val coroutinesVersion = "1.3.7"
    val eddsaVersion = "0.3.0"
    val fragmentVersion = "1.2.5"
    val jsr305Version = "3.0.2"
    val junitVersion = "4.13"
    val kotlinVersion = "1.3.72"
    val materialComponentsVersion = "1.2.0-alpha06"
    val mavenPluginVersion = "2.1"
    val preferenceVersion = "1.1.1"
    val streamsupportVersion = "1.7.2"
    val threetenabpVersion = "1.2.4"
    val zxingEmbeddedVersion = "3.6.0"

    testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.72")
    testImplementation("junit:junit:4.13")
    testImplementation("org.mockito:mockito-core:3.3.3")
    testImplementation("org.robolectric:robolectric:4.3.1")
    testImplementation("androidx.test:core:1.2.0")

    implementation("com.wireguard.android:tunnel:1.0.20200407")
    implementation("androidx.annotation:annotation:$annotationsVersion")
    implementation("androidx.appcompat:appcompat:$appcompatVersion")
    implementation("androidx.constraintlayout:constraintlayout:$constraintLayoutVersion")
    implementation("androidx.coordinatorlayout:coordinatorlayout:$coordinatorLayoutVersion")
    implementation("androidx.biometric:biometric:$biometricVersion")
    implementation("androidx.core:core-ktx:$coreKtxVersion")
    implementation("androidx.databinding:databinding-runtime:$agpVersion")
    implementation("androidx.fragment:fragment-ktx:$fragmentVersion")
    implementation("androidx.preference:preference:$preferenceVersion")
    implementation("com.google.android.material:material:$materialComponentsVersion")
    implementation("com.journeyapps:zxing-android-embedded:$zxingEmbeddedVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("net.sourceforge.streamsupport:android-retrofuture:$streamsupportVersion")

}
