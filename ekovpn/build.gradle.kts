plugins {
    id("com.onesignal.androidsdk.onesignal-gradle-plugin")
    id("com.android.application")
    id("checkstyle")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
    kotlin("android")
    kotlin("android.extensions")
}

android {
    compileSdkVersion(29)

    defaultConfig {
        applicationId  = "com.ekovpn.android"
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 10
        versionName = "1.0.6"
        resConfigs(listOf("en"))
        manifestPlaceholders = mapOf("onesignal_app_id" to "3016495d-8f12-4187-a7d2-4217b8ce7563", "onesignal_google_project_number" to "REMOTE")
    }

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_1_8
        sourceCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    signingConfigs {
        getByName("debug") {
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storeFile = rootProject.file("debug.keystore")
            storePassword = "android"
        }
        create("debuggableRelease") {
            storeFile = file("ekovpn_key.jks")
            storePassword = "Password01"
            keyAlias = "ekoVPN"
            keyPassword = "Password01"
        }
        create("release") {
            storeFile = file("ekovpn_key.jks")
            storePassword = "Password01"
            keyAlias = "ekoVPN"
            keyPassword = "Password01"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
//        create("debuggableRelease") {
//            isMinifyEnabled = false
//            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
//            signingConfig = signingConfigs.getByName("release")
//            isDebuggable = true
//        }
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = true
            //applicationIdSuffix = ".debug"
        }
    }

    flavorDimensions("implementation")

    productFlavors {
        create("ui") {
            buildConfigField("String", "IP_STACK_API_KEY", "\"c1aab8424db6187a6d0e5baa164afc13\"")
            buildConfigField("String", "IP_STACK_BASE_URL", "\"http://api.ipstack.com/\"")
            buildConfigField("String", "AWS_IP_BASE_URL", "\"http://checkip.amazonaws.com/\"")
            buildConfigField("String", "EKO_VPN_BASE_URL", "\"https://appapi.ekovpnstagingserver.com/api/v1/app/\"")
            buildConfigField("String", "ANDROID_APP_LOGIN", "\"66868\"")
            buildConfigField("String", "ANDROID_APP_PASSWORD", "\"I52nvt29\"")
            buildConfigField("String", "ADMOB_APP_ID", "\"ca-app-pub-7604868220609576~8057365177\"")
            dimension = "implementation"
            matchingFallbacks = mutableListOf("uiRelease")
        }
        create("skeleton") {
            buildConfigField("String", "IP_STACK_API_KEY", "\"c1aab8424db6187a6d0e5baa164afc13\"")
            buildConfigField("String", "IP_STACK_BASE_URL", "\"http://api.ipstack.com/\"")
            buildConfigField("String", "AWS_IP_BASE_URL", "\"http://checkip.amazonaws.com/\"")
            buildConfigField("String", "ADMOB_APP_ID", "\"ca-app-pub-7604868220609576~8057365177\"")
            buildConfigField("String", "EKO_VPN_BASE_URL", "\"https://appapi.ekovpnstagingserver.com/api/v1/app/\"")
            buildConfigField("String", "ANDROID_APP_LOGIN", "\"66868\"")
            buildConfigField("String", "ANDROID_APP_PASSWORD", "\"I52nvt29\"")
            dimension = "implementation"
            matchingFallbacks = mutableListOf("skeletonRelease")
        }
    }

    //dataBinding.isEnabled = true

}

dependencies {
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
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
    val okhttp3LoggingInterceptorVersion = "3.9.0"
    val retrofitGsonVersion = "2.3.0"
    val streamsupportVersion = "1.7.2"
    val billing_version = "3.0.0"
    val work_version = "2.4.0"


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
    implementation("com.squareup.okhttp3:logging-interceptor:${okhttp3LoggingInterceptorVersion}")
    implementation("com.squareup.retrofit2:converter-gson:${retrofitGsonVersion}")

    implementation("androidx.activity:activity-ktx:$activityVersion")
    implementation("androidx.fragment:fragment-ktx:$fragmentVersion")
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("com.google.dagger:dagger:$dagger")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines")
    implementation("io.coil-kt:coil:0.11.0")
    implementation("com.tomergoldst.android:tooltips:1.0.10")
    implementation("com.rodolfonavalon:ShapeRippleLibrary:1.0.0")

    implementation("androidx.navigation:navigation-fragment-ktx:2.3.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.0")
    implementation("androidx.core:core-ktx:1.3.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.mindorks.android:prdownloader:0.6.0")
    implementation("com.onesignal:OneSignal:3.15.2")
    implementation ("com.github.skydoves:balloon:1.1.5")
    implementation("net.sourceforge.streamsupport:android-retrofuture:$streamsupportVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.7")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("io.cabriole:decorator:1.0.0")
    implementation("com.wireguard.android:tunnel:1.0.20200407")

    implementation("com.google.firebase:firebase-analytics:17.4.4")
    implementation("com.google.firebase:firebase-crashlytics:17.1.1")
    implementation("com.google.android.gms:play-services-ads:19.3.0")
    implementation( "com.google.android.play:core-ktx:1.8.0")
    implementation("com.android.billingclient:billing-ktx:$billing_version")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation( "androidx.work:work-runtime-ktx:$work_version")

    dependencies.add("uiImplementation", project(":openvpn"))
    dependencies.add("skeletonImplementation", project(":openvpn"))
    implementation(project(":ikeV2"))
    api(project(":wireguard"))

    testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.72")
    testImplementation("junit:junit:4.13")
    testImplementation("org.mockito:mockito-core:3.3.3")
    testImplementation("org.robolectric:robolectric:4.3.1")
    testImplementation("androidx.test:core:1.2.0")

    kapt("androidx.room:room-compiler:$roomVersion")
    kapt("com.google.dagger:dagger-compiler:$dagger")

}
