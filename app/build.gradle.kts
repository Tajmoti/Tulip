@file:Suppress("PropertyName")

// Development options
val HTTP_DEBUG = false

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    id("androidx.navigation.safeargs")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

@Suppress("UnstableApiUsage")
android {
    compileSdk = 31
    buildToolsVersion = "31.0.0"

    defaultConfig {
        applicationId = "com.tajmoti.tulip"
        minSdk = 21
        targetSdk = 31
        versionCode = 17
        versionName = "0.14.0"
    }

    signingConfigs {
        rootProject.loadPropsIfExists("signing.properties")?.let { signingProps ->
            create("release") {
                storeFile = rootProject.file(signingProps.getProperty("signing.store.file")!!)
                storePassword = signingProps.getProperty("signing.store.password")!!
                keyAlias = signingProps.getProperty("signing.key.alias")!!
                keyPassword = signingProps.getProperty("signing.key.password")!!
            }
        }
    }

    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("boolean", "HTTP_DEBUG", "false")
            signingConfig = signingConfigs.findByName("release")
        }
        debug {
            isDebuggable = true
            buildConfigField("boolean", "HTTP_DEBUG", "$HTTP_DEBUG")
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        @Suppress("SuspiciousCollectionReassignment")
        freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

secrets {
    defaultPropertiesFileName = "default.secrets.properties"
    propertiesFileName = "secrets.properties"
}

repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":commonutils"))

    implementation(project(":tulip:libtulip"))
    implementation(project(":tulip:libtulip-ui"))

    implementation(project(":tulip:libtulip-api"))
    implementation(project(":tulip:libtulip-persistence-api"))
    implementation(project(":tulip:libtulip-persistence-android"))
    implementation(project(":tulip:libtulip-ui-api"))

    implementation(project(":libopensubtitles"))
    implementation(project(":webdriver:libwebdriver"))
    implementation(project(":webdriver:libwebdriver-android"))

    with(Versions.Kotlin) {
        implementation(coroutinesCore)
        implementation(coroutinesAndroid)
    }
    with(Versions.Android.Core) {
        implementation(coreKtx)
    }
    with(Versions.Android.AppCompat) {
        implementation(appCompat)
    }
    with(Versions.Android.Material) {
        implementation(materialCore)
    }
    with(Versions.Android.Lifecycle) {
        implementation(livedataKtx)
        implementation(viewModelKtx)
        implementation(runtimeKtx)
    }
    with(Versions.Android.Nav) {
        implementation(fragmentKtx)
        implementation(uiKtx)
        implementation(dynamicFeaturesFragment)
    }
    with(Versions.Android.ConstraintLayout) {
        implementation(core)
    }
    with(Versions.Android.Hilt) {
        implementation(hiltAndroid)
        implementation(hiltNavigationFragment)
        kapt(hiltAndroidCompiler)
    }
    with(Versions.Android.Support) {
        implementation(legacySupport)
    }
    with(Versions.Android.Room) {
        implementation(roomRuntime)
        implementation(roomKtx)
        kapt(roomCompiler)
    }

    with(Versions.Android.Groupie) {
        implementation(groupie)
        implementation(groupieViewBinding)
        implementation(groupieDataBinding)
    }

    with(Versions.Ktor) {
        implementation(clientOkhttp)
    }

    implementation("com.github.tony19:logback-android:2.0.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("org.videolan.android:libvlc-all:3.4.8")
}