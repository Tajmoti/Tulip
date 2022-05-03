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
    }
}

fun KotlinDependencyHandler.mainDeps() {
    implementation(project(":commonutils"))

    with(Versions.Kotlin) {
        implementation(coroutinesCore)
    }
}