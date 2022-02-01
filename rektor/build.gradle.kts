plugins {
    kotlin("multiplatform")
}

group = "com.tajmoti"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }
    js(IR) {
        browser()
    }
    sourceSets {
        val commonMain by getting

        val ktor = "2.0.0-beta-1"

        sourceSets["commonMain"].dependencies {
            implementation("io.ktor:ktor-client-core:${ktor}")
        }
        sourceSets["commonTest"].dependencies {
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")
        }
        sourceSets["jvmTest"].dependencies {
            implementation("junit:junit:4.13.1")
            implementation("org.mockito:mockito-all:1.10.19")
            implementation("org.jetbrains.kotlin:kotlin-test-junit:1.6.10")
        }
    }
}
