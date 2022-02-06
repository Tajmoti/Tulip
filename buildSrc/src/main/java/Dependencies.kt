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
    private const val moshiAdapter = "2.9.0"


    object Ktor {
        const val core = "io.ktor:ktor-client-core:$ktor"
        const val clientJvm = "io.ktor:ktor-client-okhttp:$ktor"
        const val clientJs = "io.ktor:ktor-client-js:$ktor"
    }

    object OkHttp {
        const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:$okHttpLogger"
        const val moshi = "com.squareup.retrofit2:converter-moshi:$moshiAdapter"
    }

    object Arrow {
        const val core = "io.arrow-kt:arrow-core:$arrow"
    }

    object KotlinTestJUnit {
        const val testJunit = "org.jetbrains.kotlin:kotlin-test-junit:$kt"
    }

    object Store {
        const val core = "com.dropbox.mobile.store:store4:$store"
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
    }
}