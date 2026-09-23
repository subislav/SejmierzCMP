package pl.jsubczynski.sejmierz.app.benchmark

import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import pl.jsubczynski.sejmierz.domain.benchmark.MetricTag
import pl.jsubczynski.sejmierz.domain.benchmark.RunContextProvider
import java.util.UUID

class AndroidRunContextProvider(
    private val context: Context,
) : RunContextProvider {

    private val runId: String = UUID.randomUUID().toString().take(8)

    override fun globalTags(): Map<String, String> = mapOf(
        MetricTag.PLATFORM to "CMP-Android",
        MetricTag.DEVICE to "${Build.MANUFACTURER} ${Build.MODEL}",
        MetricTag.OS_VERSION to "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
        MetricTag.BUILD_TYPE to buildType(),
        MetricTag.NETWORK_TYPE to networkType(),
        MetricTag.RUN_ID to runId,
    )

    private fun buildType(): String =
        if ((context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) "debug" else "release"

    private fun networkType(): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return "UNKNOWN"
        val network = cm.activeNetwork ?: return "NONE"
        val caps = cm.getNetworkCapabilities(network) ?: return "NONE"
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "CELLULAR"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET"
            else -> "OTHER"
        }
    }
}
