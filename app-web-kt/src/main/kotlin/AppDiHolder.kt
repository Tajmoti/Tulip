import com.tajmoti.libtulip.di.tulipModule
import com.tajmoti.multiplatform.jsNetworkModule
import org.koin.core.Koin
import org.koin.mp.KoinPlatformTools

object AppDiHolder {
    val di = setupDI()

    private fun setupDI(): Koin {
        val ctx = KoinPlatformTools.defaultContext()
        ctx.startKoin { modules(tulipModule + jsNetworkModule) }
        return ctx.get()
    }
}