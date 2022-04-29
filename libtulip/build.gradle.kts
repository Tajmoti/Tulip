import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.codingfeline.buildkonfig.gradle.TargetConfigDsl
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

plugins {
    kotlin("multiplatform")
    kotlin("kapt")
    kotlin("plugin.serialization")
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
    }
}

buildkonfig {
    packageName = "com.tajmoti.libtulip"

    defaultConfigs {
        addSecretFromEnvOrFile("tmdbApiKey", "TMDB_API_KEY")
        addSecretFromEnvOrFile("openSubtitlesApiKey", "OPENSUBTITLES_API_KEY")
        buildConfigField(FieldSpec.Type.STRING, "commit", getGitCommitHash())
    }
}

fun KotlinDependencyHandler.mainDeps() {
    implementation(project(":commonutils"))

    implementation(project(":libtulip-api"))
    implementation(project(":libtulip-ui-api"))
    implementation(project(":libtulip-persistence-api"))

    implementation(project(":libtulip-ui"))
    implementation(project(":libtulip-service"))

    implementation(project(":libopensubtitles"))
    implementation(project(":libtvprovider"))
    implementation(project(":libtvprovider-kinox"))
    implementation(project(":libtvprovider-primewire"))
    implementation(project(":libtvprovider-southpark"))
    implementation(project(":libtvvideoextractor"))
    implementation(project(":libtmdb"))
    implementation(project(":libwebdriver"))
    implementation(project(":rektor"))

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

fun KotlinDependencyHandler.jsDeps() {
    with(Versions.Ktor) {
        implementation(clientJs)
    }
}

fun TargetConfigDsl.addSecretFromEnvOrFile(name: String, envName: String) {
    buildConfigField(FieldSpec.Type.STRING, name, getEnvOrLocalSecret(rootProject, envName, name))
}