plugins {
    id("java-library")
    id("kotlin")
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

dependencies {
    api(project(":libtvprovider"))
    api(project(":commonutils"))

    implementation(Versions.Jsoup.core)
    implementation(Versions.Kotlin.coroutinesCore)
}