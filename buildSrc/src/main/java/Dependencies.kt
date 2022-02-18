object Versions {
    private const val arrow = "1.0.1"
    private const val ktor = "2.0.0-beta-1"
    private const val kt = "1.6.10"
    private const val store = "4.0.4-KT15"
    private const val junitJupiter = "5.8.2"
    private const val junitSuiteVer = "1.8.2"
    private const val mockito = "4.2.0"
    private const val mockitoKotlin = "4.0.0"
    private const val slf4jSimple = "1.7.32"
    private const val okHttpLogger = "4.9.3"
    private const val dagger = "2.40.5"
    private const val coroutinesVer = "1.6.0"
    private const val kotlinLoggingVer = "2.1.21"
    private const val kotlinSerialization = "1.3.2"
    private const val kotlin = "1.6.20-M1"
    private const val jsoup = "1.14.3"
    private const val koin = "3.1.5"
    private const val buildKonfig = "0.11.0"


    object Kotlin {
        const val plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin"
        const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVer"
        const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVer"
        const val serializationJson = "org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerialization"
        const val serializationPlugin = "org.jetbrains.kotlin:kotlin-serialization:$kotlin"
    }

    object KotlinLogging {
        const val kotlinLogging = "io.github.microutils:kotlin-logging:$kotlinLoggingVer"
    }

    object Ktor {
        const val core = "io.ktor:ktor-client-core:$ktor"
        const val json = "io.ktor:ktor-serialization-kotlinx-json:$ktor"
        const val contentNegotiation = "io.ktor:ktor-client-content-negotiation:$ktor"
        const val clientJvm = "io.ktor:ktor-client-okhttp:$ktor"
        const val clientJs = "io.ktor:ktor-client-js:$ktor"
        const val clientOkhttp = "io.ktor:ktor-client-okhttp:$ktor"
    }


    object Koin {
        const val core = "io.insert-koin:koin-core:$koin"
    }

    object OkHttp {
        const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:$okHttpLogger"
    }

    object Arrow {
        const val core = "io.arrow-kt:arrow-core:$arrow"
    }

    object KotlinTestJUnit {
        const val testJunit = "org.jetbrains.kotlin:kotlin-test-junit:$kt"
        const val testJs = "org.jetbrains.kotlin:kotlin-test-js:$kt"
    }

    object Store {
        const val storeJvm = "com.dropbox.mobile.store:store4:$store"
    }

    object JvmTest {
        const val jupiterApi = "org.junit.jupiter:junit-jupiter-api:$junitJupiter"
        const val jupiterEngine = "org.junit.jupiter:junit-jupiter-engine:$junitJupiter"
        const val junitSuite = "org.junit.platform:junit-platform-suite:$junitSuiteVer"
        const val mockitoCore = "org.mockito:mockito-core:$mockito"
        const val mockitoKt = "org.mockito.kotlin:mockito-kotlin:$mockitoKotlin"
        const val slf4j = "org.slf4j:slf4j-simple:$slf4jSimple"
    }

    object JvmDi {
        const val inject = "javax.inject:javax.inject:1"
        const val daggerCore = "com.google.dagger:dagger:$dagger"
        const val daggerCompiler = "com.google.dagger:dagger-compiler:$dagger"
    }

    object Jsoup {
        const val core = "org.jsoup:jsoup:$jsoup"
    }

    object BuildKonfig {
        const val core = "com.codingfeline.buildkonfig:buildkonfig-gradle-plugin:$buildKonfig"
    }

    object Android {
        private const val nav = "2.4.0"
        private const val hilt = "2.40.5"
        private const val hiltNavigationFragmentVer = "1.0.0"
        private const val gradleSecrets = "2.0.0"
        private const val room = "2.4.1"
        private const val legacySupportVer = "1.0.0"
        private const val groupieVer = "2.9.0"
        private const val constraintLayout = "2.1.3"
        private const val lifecycle = "2.4.0"
        private const val androidxCore = "1.7.0"
        private const val appCompatVer = "1.4.1"
        private const val materialCoreVer = "1.5.0"


        object Core {
            const val coreKtx = "androidx.core:core-ktx:$androidxCore"
        }
        object AppCompat {
            const val appCompat = "androidx.appcompat:appcompat:$appCompatVer"
        }
        object Material {
            const val materialCore = "com.google.android.material:material:$materialCoreVer"
        }
        object Hilt {
            const val plugin = "com.google.dagger:hilt-android-gradle-plugin:$hilt"
            const val hiltAndroid = "com.google.dagger:hilt-android:$hilt"
            const val hiltAndroidCompiler = "com.google.dagger:hilt-android-compiler:$hilt"
            const val hiltNavigationFragment = "androidx.hilt:hilt-navigation-fragment:$hiltNavigationFragmentVer"
        }
        object Nav {
            const val safeArgsPlugin = "androidx.navigation:navigation-safe-args-gradle-plugin:$nav"
            const val fragmentKtx = "androidx.navigation:navigation-fragment-ktx:$nav"
            const val uiKtx = "androidx.navigation:navigation-ui-ktx:$nav"
            const val dynamicFeaturesFragment = "androidx.navigation:navigation-dynamic-features-fragment:$nav"
        }
        object Room {
            const val roomRuntime = "androidx.room:room-runtime:$room"
            const val roomKtx = "androidx.room:room-ktx:$room"
            const val roomCompiler = "androidx.room:room-compiler:$room"
        }
        object Support {
            const val legacySupport = "androidx.legacy:legacy-support-v4:$legacySupportVer"
        }
        object Lifecycle {
            const val livedataKtx = "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle"
            const val viewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle"
            const val runtimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle"
        }
        object ConstraintLayout {
            const val core = "androidx.constraintlayout:constraintlayout:$constraintLayout"
        }
        object Groupie {
            const val groupie = "com.github.lisawray.groupie:groupie:$groupieVer"
            const val groupieViewBinding = "com.github.lisawray.groupie:groupie-viewbinding:$groupieVer"
            const val groupieDataBinding = "com.github.lisawray.groupie:groupie-databinding:$groupieVer"
        }
        object GradleSecrets {
            const val plugin = "com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:$gradleSecrets"
        }
    }
}