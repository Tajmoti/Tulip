import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.codingfeline.buildkonfig.gradle.TargetConfigDsl
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

plugins {
    kotlin("multiplatform")
    kotlin("kapt")
    id("com.codingfeline.buildkonfig")
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
        sourceSets["jsMain"].dependencies { jsDeps() }
        sourceSets["jvmTest"].dependencies { jvmTestDeps() }
    }
}

buildkonfig {
    packageName = "com.tajmoti.libtulip"

    defaultConfigs {
        addSecretFromEnvOrFile("tmdbApiKey", "TMDB_API_KEY")
        addSecretFromEnvOrFile("openSubtitlesApiKey", "OPENSUBTITLES_API_KEY")
    }
}

fun KotlinDependencyHandler.mainDeps() {
    implementation(project(":libopensubtitles"))
    implementation(project(":libtmdb"))
    implementation(project(":libtvprovider"))
    implementation(project(":libtvprovider-kinox"))
    implementation(project(":libtvprovider-primewire"))
    implementation(project(":libtvprovider-southpark"))
    implementation(project(":libtvvideoextractor"))
    implementation(project(":libwebdriver"))
    implementation(project(":rektor"))
    implementation(project(":commonutils"))

    with(Versions.Arrow) {
        implementation(core)
    }
    with(Versions.Kotlin) {
        implementation(coroutinesCore)
    }
    with(Versions.Ktor) {
        implementation(core)
        implementation(json)
        implementation(contentNegotiation)
    }
    with(Versions.Koin) {
        implementation(core)
    }
}

fun KotlinDependencyHandler.jvmDeps() {
    implementation(project(":libtvprovider-kinox"))

    with(Versions.Ktor) {
        implementation(clientJvm)
    }
    with(Versions.OkHttp) {
        implementation(loggingInterceptor)
    }
    with(Versions.Store) {
        implementation(storeJvm)
    }
    with(Versions.JvmDi) {
        implementation(inject)
        implementation(daggerCore)
        configurations["kapt"].dependencies.add(project.dependencies.create(daggerCompiler))
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

fun KotlinDependencyHandler.jsDeps() {
    with(Versions.Ktor) {
        implementation(clientJs)
    }
}

fun TargetConfigDsl.addSecretFromEnvOrFile(name: String, envName: String) {
    buildConfigField(FieldSpec.Type.STRING, name, getEnvOrLocalSecret(rootProject, envName, name))
}