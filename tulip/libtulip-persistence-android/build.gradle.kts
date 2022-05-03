plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

@Suppress("UnstableApiUsage")
android {
    compileSdk = 31
    buildToolsVersion = "31.0.0"

    defaultConfig {
        minSdk = 21
        targetSdk = 31

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        forEach { buildType ->
            buildType.javaCompileOptions {
                annotationProcessorOptions {
                    argument("room.schemaLocation", "$projectDir/schemas")
                }
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":commonutils"))

    implementation(project(":tulip:libtulip-api"))
    implementation(project(":tulip:libtulip-persistence-api"))

    implementation(Versions.Kotlin.coroutinesCore)

    compileOnly(Versions.JavaX.inject)

    with(Versions.Android.Room) {
        implementation(roomRuntime)
        implementation(roomKtx)
        kapt(roomCompiler)
    }
}