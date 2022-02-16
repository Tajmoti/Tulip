import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

plugins {
    kotlin("multiplatform")
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
        sourceSets["commonMain"].dependencies { mainDeps() }
        sourceSets["jvmMain"].dependencies { jvmDeps() }
    }
}

fun KotlinDependencyHandler.jvmDeps() {
    with(Versions.Jsoup) {
        implementation(core)
    }
}
fun KotlinDependencyHandler.mainDeps() {
    with(Versions.Kotlin) {
        implementation(coroutinesCore)
    }
    with(Versions.KotlinLogging) {
        api(kotlinLogging)
    }
}