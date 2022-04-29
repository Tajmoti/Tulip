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
    }
}

fun KotlinDependencyHandler.mainDeps() {
    implementation(project(":libtulip-api"))
    with(Versions.Kotlin) {
        implementation(coroutinesCore)
    }
}
