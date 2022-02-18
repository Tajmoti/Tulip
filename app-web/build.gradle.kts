plugins {
    kotlin("js")
}

dependencies {
    implementation(project(":libtulip"))
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.3")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-pre.240-kotlin-1.5.30")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:17.0.2-pre.240-kotlin-1.5.30")
}

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }
}
