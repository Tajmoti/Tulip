import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

plugins {
    kotlin("multiplatform")
    kotlin("kapt")
    kotlin("plugin.serialization")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
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
        all { languageSettings.optIn("kotlin.RequiresOptIn") }
        sourceSets["commonMain"].dependencies { mainDeps() }
        sourceSets["jvmMain"].dependencies { jvmDeps() }
    }
}

fun KotlinDependencyHandler.mainDeps() {
    implementation(project(":commonutils"))

    implementation(project(":tulip:libtulip-api"))
    implementation(project(":tulip:libtulip-persistence-api"))

    implementation(project(":libopensubtitles"))
    implementation(project(":libtmdb"))
    implementation(project(":tvprovider:libtvprovider"))
    implementation(project(":libtvvideoextractor"))

    implementation(Versions.Kotlin.serializationJson)
    with(Versions.Kotlin) {
        implementation(coroutinesCore)
    }
    with(Versions.Arrow) {
        implementation(core)
    }
    with(Versions.Ktor) {
        implementation(core)
    }
}

fun KotlinDependencyHandler.jvmDeps() {
    with(Versions.Store) {
        implementation(storeJvm)
    }
}