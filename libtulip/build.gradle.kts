import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

plugins {
    kotlin("multiplatform")
    kotlin("kapt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    jvm {
        compilations.all { kotlinOptions.jvmTarget = "11" }
        withJava()
    }
    js(IR) {
        browser()
    }
    sourceSets {
        sourceSets["jvmMain"].dependencies { mainDeps() }
        sourceSets["jvmMain"].dependencies { jvmDeps() }
        sourceSets["jvmTest"].dependencies { jvmTestDeps() }
    }
}

fun KotlinDependencyHandler.mainDeps() {
    implementation(project(":libtvprovider"))
    implementation(project(":libtvprovider-kinox"))
    implementation(project(":libtvprovider-primewire"))
    implementation(project(":libtvprovider-southpark"))
    implementation(project(":libwebdriver"))
    implementation(project(":libtvvideoextractor"))
    implementation(project(":libtmdb"))
    implementation(project(":libopensubtitles"))

    with(Versions.Ktor) {
        implementation(core)
    }
    with(Versions.Arrow) {
        implementation(core)
    }
    with(Versions.Store) {
        implementation(core)
    }
}

fun KotlinDependencyHandler.jvmDeps() {
    with(Versions.Ktor) {
        implementation(clientJvm)
    }
    with(Versions.OkHttp) {
        implementation(loggingInterceptor)
        implementation(moshi)
    }
    with(Versions.JvmDi) {
        implementation(inject)
        implementation(daggerCore)
        configurations["kapt"].dependencies.add(project.dependencies.create("com.google.dagger:dagger-compiler:2.40.5"))
    }
}

fun KotlinDependencyHandler.jvmTestDeps() {
    with(Versions.JvmTest) {
        implementation(jupiterApi)
        implementation(junitSuite)
        implementation(mockitoCore)
        implementation(mockitoKt)
        implementation(slf4j)
        implementation(jupiterEngine)
    }
}