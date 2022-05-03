plugins {
    kotlin("js")
}

dependencies {
    implementation(project(":tulip:libtulip"))
    implementation(project(":tulip:libtulip-ui"))
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-legacy:17.0.2-pre.302-kotlin-1.6.10")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom-legacy:17.0.2-pre.302-kotlin-1.6.10")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:6.2.1-pre.302-kotlin-1.6.10")
}

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }
}
