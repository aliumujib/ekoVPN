/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */
plugins {
    id("com.android.library")
    id("checkstyle")
    kotlin("android")
    kotlin("android.extensions")
}

val openvpn3SwigFiles = File(buildDir, "generated/source/ovpn3swig/ovpn3")

tasks.register<Exec>("generateOpenVPN3Swig") {
    var swigcmd = "swig"
    // Workaround for Mac OS X since it otherwise does not find swig and I cannot get
    // the Exec task to respect the PATH environment :(
    if (File("/usr/local/bin/swig").exists())
        swigcmd = "/usr/local/bin/swig"
//    else
//        print("File not found")

    doFirst {
        mkdir(openvpn3SwigFiles)
    }
    commandLine(listOf(swigcmd, "-outdir", openvpn3SwigFiles, "-outcurrentdir", "-c++", "-java", "-package", "net.openvpn.ovpn3",
            "-Isrc/main/cpp/openvpn3/client", "-Isrc/main/cpp/openvpn3/",
            "-o", "${openvpn3SwigFiles}/ovpncli_wrap.cxx", "-oh", "${openvpn3SwigFiles}/ovpncli_wrap.h",
            "src/main/cpp/openvpn3/javacli/ovpncli.i"))
}

val swigTask = tasks.named("generateOpenVPN3Swig")
val preBuildTask = tasks.getByName("preBuild")
val assembleTask = tasks.getByName("assemble")

println(tasks.names)

assembleTask.dependsOn(swigTask)
preBuildTask.dependsOn(swigTask)

android {
    compileSdkVersion(29)

    defaultConfig {
        minSdkVersion(14)
        targetSdkVersion(29)
        versionCode = 169
        versionName = "0.7.16"
    }

    //testOptions.unitTests.isIncludeAndroidResources = true

    externalNativeBuild {
        cmake {
            setPath(File("${projectDir}/src/main/cpp/CMakeLists.txt"))
        }
    }

    sourceSets {
        getByName("main") {
            assets.srcDirs("src/main/assets", "build/ovpnassets")
            java.srcDirs("src/ovpn3/java/", openvpn3SwigFiles)
        }

        create("ui") {
            assets.srcDirs("src/main/assets", "build/ovpnassets")
            java.srcDirs("src/ovpn3/java/", openvpn3SwigFiles)
        }

        create("skeleton") {

        }

        getByName("debug") {

        }

        getByName("release") {

        }
    }


    flavorDimensions("implementation")

    productFlavors {
        defaultPublishConfig = "uiRelease"

        create("ui") {
            setDimension("implementation")
            buildConfigField("boolean", "openvpn3", "true")
        }
        create("skeleton") {
            setDimension("implementation")
            buildConfigField("boolean", "openvpn3", "false")
        }
    }

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_1_8
        sourceCompatibility = JavaVersion.VERSION_1_8
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
            isUniversalApk = true
        }
    }
}


dependencies {
    // https://maven.google.com/web/index.html
    // https://developer.android.com/jetpack/androidx/releases/core
    val preferenceVersion = "1.1.1"
    val coreVersion = "1.2.0"
    val materialVersion = "1.1.0"
    val fragmentVersion = "1.2.4"

    implementation("androidx.annotation:annotation:1.1.0")
    implementation("androidx.core:core:$coreVersion")
    implementation("androidx.annotation:annotation:1.1.0")
    implementation( "androidx.constraintlayout:constraintlayout:1.1.3")
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
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")

    testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.72")
    testImplementation("junit:junit:4.13")
    testImplementation("org.mockito:mockito-core:3.3.3")
    testImplementation("org.robolectric:robolectric:4.3.1")
    testImplementation("androidx.test:core:1.2.0")

}
