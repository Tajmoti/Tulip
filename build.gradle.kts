buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath(Versions.Kotlin.plugin)
        classpath(Versions.Kotlin.serializationPlugin)
        classpath(Versions.Android.Hilt.plugin)
        classpath(Versions.Android.Nav.safeArgsPlugin)
        classpath(Versions.Android.GradleSecrets.plugin)
        classpath(Versions.BuildKonfig.core)
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
