plugins {
    kotlin("multiplatform")
}

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
        sourceSets["commonMain"].dependencies {
            with(Versions.Ktor) {
                implementation(core)
            }
            with(Versions.KotlinLogging) {
                implementation(kotlinLogging)
            }
        }
        sourceSets["commonTest"].dependencies {
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")
        }
        sourceSets["jvmTest"].dependencies {
            with(Versions.KotlinTestJUnit) {
                implementation(testJunit)
            }
        }
        sourceSets["jsTest"].dependencies {
            with(Versions.KotlinTestJUnit) {
                implementation(testJs)
            }
        }
    }
}
